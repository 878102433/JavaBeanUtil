package application.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import application.MainApp;
import application.model.DbColumn;
import application.model.ForeignColumn;
import application.model.JavaColumn;
import application.model.JavaDB;
import application.model.SqlTypeDefeat;
import application.model.TableBean;
import application.model.TableFile;
import application.util.ConnectionFactory;
import application.util.DialogUtil;
import application.util.FileUtil;
import application.util.FormatSql;
import application.util.PropertiesUtil;
import application.util.StringUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;

public class DatabaseController {
	Logger logger = Logger.getLogger(this.getClass().getName());
	@FXML
	private TextField url;
	@FXML
	private TextField userName;
	@FXML
	private PasswordField password;
	@FXML
	private TextField packageDirectory;
	@FXML
	private TextField tablePrefix;
	@FXML
	private TextField packagePrefix;
	@FXML
	private Label databaseType;
	@FXML
	private TreeView<TableBean> tableTreeView;
	@FXML
	private TreeView<TableBean> beanTreeView;

	@FXML
	private TableView<ForeignColumn> tableView;
	@FXML
	private TableColumn<ForeignColumn, Boolean> checkedColumn;
	@FXML
	private TableColumn<ForeignColumn, String> tableNameColumn;
	@FXML
	private TableColumn<ForeignColumn, String> columnNameColumn;
	@FXML
	private TableColumn<ForeignColumn, String> foreignTableNameColumn;
	@FXML
	private TableColumn<ForeignColumn, String> foreignColumnNameColumn;
	private MainApp mainApp;

	private final String TYPE_DRAG_TABLE = "TYPE_DRAG_TABLE";
	private final String TYPE_DRAG_BEAN = "TYPE_DRAG_BEAN";
	private static final String rootTableIcon = "file:resources/images/database_16.png";
	private static final String tableIcon = "file:resources/images/table_16.png";
	private static final String tableUsedIcon = "file:resources/images/table_used_16.png";
	private static final String rootBeanIcon = "file:resources/images/package_source_16.png";
	private static final String packageIcon = "file:resources/images/package_16.png";
	private static final String packageEmptyIcon = "file:resources/images/package_empty_16.png";
	private static final String beanIcon = "file:resources/images/java.png";
	private List<SqlTypeDefeat> sqlTypeDefeats;
	DataFormat dataFormatType = null;
	DataFormat dataFormatTableNames = null;
	int packgeCount = 0;
	private String packageSuffix = "po";

	private List<TreeCell<TableBean>> nodes = new ArrayList<TreeCell<TableBean>>();

	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}

	@FXML
	private void initialize() {
		try {
			PropertyConfigurator.configure("resources/config/log4j.properties");
			url.setText(PropertiesUtil.getProperty("dbUrl"));
			userName.setText(PropertiesUtil.getProperty("dbUserName"));
			packageDirectory.setText(PropertiesUtil.getProperty("packageDirectory"));
			packagePrefix.setText(PropertiesUtil.getProperty("packagePrefix"));
			packageSuffix = PropertiesUtil.getProperty("packageSuffix");
			dataFormatType = DataFormat.lookupMimeType("type") == null ? new DataFormat("type") : DataFormat.lookupMimeType("type");
			dataFormatTableNames = DataFormat.lookupMimeType("tableNames") == null ? new DataFormat("tableNames") : DataFormat.lookupMimeType("tableNames");
			initSqlTypeDefeat();

			tableNameColumn.setCellValueFactory(cellData -> cellData.getValue().tableNameProperty());
			columnNameColumn.setCellValueFactory(cellData -> cellData.getValue().columnNameProperty());
			foreignTableNameColumn.setCellValueFactory(cellData -> cellData.getValue().foreignTableNameProperty());
			foreignColumnNameColumn.setCellValueFactory(cellData -> cellData.getValue().foreignColumnNameProperty());
			checkedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(checkedColumn));
			checkedColumn.setCellValueFactory(cellData -> cellData.getValue().isCheckedProperty());

		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		createTree();
	}

	private void createTree() {
		TreeItem<TableBean> rootTableItem = new TreeItem<>(new TableBean(TableBean.TYPE_TABLE_ROOT, "表"), new ImageView(new Image(rootTableIcon)));
		rootTableItem.setExpanded(true);
		tableTreeView.setRoot(rootTableItem);
		tableTreeView.setEditable(true);
		tableTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		tableTreeView.setCellFactory((TreeView<TableBean> p) -> new TreeCellImpl());

		tableTreeView.setOnDragDetected(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				ObservableList<TreeItem<TableBean>> treeItems = tableTreeView.getSelectionModel().getSelectedItems();
				if (treeItems.size() > 0) {
					/*  检测到拖动，开始一个拖放手势 */
					/*  允许所有的传输模式 */
					Dragboard db = tableTreeView.startDragAndDrop(TransferMode.ANY);
					List<String> tableNames = new ArrayList<String>();
					WritableImage image = null;
					for (TreeItem<TableBean> treeItem : treeItems) {
						if (TableBean.TYPE_TABLE.equals(treeItem.getValue().getType()) && !treeItem.getValue().isUsed()) {
							tableNames.add(treeItem.getValue().getName());
							WritableImage writeImageText = new Text(treeItem.getValue().getName()).snapshot(new SnapshotParameters(), null);
							WritableImage writeImageIcon = treeItem.getGraphic().snapshot(new SnapshotParameters(), null);
							WritableImage writeImage = new WritableImage((int) (writeImageText.getWidth() + writeImageIcon.getWidth()), writeImageText.getHeight() > writeImageIcon.getHeight() ? (int) writeImageText.getHeight() : (int) writeImageIcon.getHeight());

							for (int x = 0; x < (int) writeImageIcon.getWidth(); x++) {
								for (int y = 0; y < (int) writeImageIcon.getHeight(); y++) {
									writeImage.getPixelWriter().setColor(x, y, writeImageIcon.getPixelReader().getColor(x, y));
								}
							}
							for (int i = 0; i < (int) writeImageText.getWidth(); i++) {
								for (int j = 0; j < (int) writeImageText.getHeight(); j++) {
									writeImage.getPixelWriter().setColor(i + (int) writeImageIcon.getWidth(), j, writeImageText.getPixelReader().getColor(i, j));
								}
							}

							if (image == null) {
								image = writeImage;
							} else {
								WritableImage imageTemp = image;
								image = new WritableImage(imageTemp.getWidth() > writeImage.getWidth() ? (int) imageTemp.getWidth() : (int) writeImage.getWidth(), (int) (imageTemp.getHeight() + writeImage.getHeight()));
								imageTemp.getPixelReader().getColor((int) (imageTemp.getWidth() - 1), (int) (imageTemp.getHeight() - 1));
								for (int x = 0; x < (int) imageTemp.getWidth(); x++) {
									for (int y = 0; y < (int) imageTemp.getHeight(); y++) {
										image.getPixelWriter().setColor(x, y, imageTemp.getPixelReader().getColor(x, y));
									}
								}
								for (int i = 0; i < (int) writeImage.getWidth(); i++) {
									for (int j = 0; j < (int) writeImage.getHeight(); j++) {
										image.getPixelWriter().setColor(i, (int) (j + imageTemp.getHeight()), writeImage.getPixelReader().getColor(i, j));
									}
								}
							}

						}
					}
					// WritableImage image = tableTreeView.snapshot(new
					// SnapshotParameters(), null);
					if (image != null) {
						db.setDragView(image);
						/*  将一个字符串放到Dragboard中  */
						ClipboardContent content = new ClipboardContent();
						content.put(dataFormatType, TYPE_DRAG_TABLE);
						content.put(dataFormatTableNames, StringUtil.list2Str(tableNames, ","));
						db.setContent(content);
					}
				}
				event.consume();
			}
		});

		tableTreeView.setOnDragDone(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				/* the drag-and-drop gesture ended */
				System.out.println("onDragDone");
				/* if the data was successfully moved, clear it */
				if (event.getTransferMode() == TransferMode.MOVE) {
					event.getDragboard().clear();
				}

				event.consume();
			}
		});

		tableTreeView.setOnDragOver(new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {

				if (TYPE_DRAG_BEAN.equals(event.getDragboard().getContent(dataFormatType)) && event.getDragboard().getContent(dataFormatTableNames) != null) {

					System.out.println("tableTreeViewonDragOver");

					event.acceptTransferModes(TransferMode.MOVE);
				}

				event.consume();
			}
		});

		tableTreeView.setOnDragDropped(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				/* data dropped */

				/*
				 * if there is a string data on dragboard, read it and use it
				 */
				Dragboard db = event.getDragboard();
				boolean success = false;

				if (TYPE_DRAG_BEAN.equals(event.getDragboard().getContent(dataFormatType)) && event.getDragboard().getContent(dataFormatTableNames) != null) {
					System.out.println("onDragDropped");
					System.out.println(db.getContent(dataFormatTableNames));
					String[] array = db.getContent(dataFormatTableNames).toString().split(",");
					for (String tableName : array) {

						TreeItem<TableBean> itemBeanTable = findTableItemByName(tableTreeView.getRoot().getChildren(), tableName);
						itemBeanTable.getValue().setUsed(false);
						itemBeanTable.setGraphic(new ImageView(new Image(tableIcon)));
						TreeItem<TableBean> itemBean = findTableItemByLinkName(beanTreeView.getRoot().getChildren(), tableName);
						TreeItem<TableBean> parent = itemBean.getParent();

						ObservableList<TreeItem<TableBean>> childrens = itemBean.getParent().getChildren();
						childrens.remove(itemBean);
						if (childrens.size() == 0) {
							parent.setGraphic(new ImageView(new Image(packageEmptyIcon)));
						}
					}
					beanTreeView.refresh();
					tableTreeView.refresh();
					success = true;
				}
				/*
				 * let the source know whether the string was successfully
				 * transferred and used
				 */
				event.setDropCompleted(success);

				event.consume();
			}
		});

		TreeItem<TableBean> rootBeanItem = new TreeItem<>(new TableBean(TableBean.TYPE_PACKAGE_ROOT, "包"), new ImageView(new Image(rootBeanIcon)));
		rootBeanItem.setExpanded(true);

		beanTreeView.setRoot(rootBeanItem);
		beanTreeView.setEditable(true);
		beanTreeView.setCellFactory((TreeView<TableBean> p) -> new TextFieldTreeCellImpl());
		beanTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		beanTreeView.setOnDragDetected(new EventHandler<MouseEvent>() {

			public void handle(MouseEvent event) {
				ObservableList<TreeItem<TableBean>> treeItems = beanTreeView.getSelectionModel().getSelectedItems();
				if (treeItems.size() > 0) {
					/*  检测到拖动，开始一个拖放手势 */
					/*  允许所有的传输模式 */
					Dragboard db = tableTreeView.startDragAndDrop(TransferMode.ANY);
					List<String> tableNames = new ArrayList<String>();
					WritableImage image = null;
					ObservableList<TreeItem<TableBean>> treeItemBeans = FXCollections.observableArrayList();
					for (TreeItem<TableBean> treeItem : treeItems) {
						if (TableBean.TYPE_BEAN.equals(treeItem.getValue().getType())) {
							treeItemBeans.add(treeItem);
						} else if (TableBean.TYPE_PACKAGE.equals(treeItem.getValue().getType()) && !treeItem.isExpanded() && treeItem.getChildren().size() > 0) {
							treeItemBeans.addAll(treeItem.getChildren());
						}

					}
					for (TreeItem<TableBean> treeItem : treeItemBeans) {
						tableNames.add(treeItem.getValue().getLinkName());
						WritableImage writeImageText = new Text(treeItem.getValue().getName()).snapshot(new SnapshotParameters(), null);
						WritableImage writeImageIcon = treeItem.getGraphic().snapshot(new SnapshotParameters(), null);
						WritableImage writeImage = new WritableImage((int) (writeImageText.getWidth() + writeImageIcon.getWidth()), writeImageText.getHeight() > writeImageIcon.getHeight() ? (int) writeImageText.getHeight() : (int) writeImageIcon.getHeight());

						for (int x = 0; x < (int) writeImageIcon.getWidth(); x++) {
							for (int y = 0; y < (int) writeImageIcon.getHeight(); y++) {
								writeImage.getPixelWriter().setColor(x, y, writeImageIcon.getPixelReader().getColor(x, y));
							}
						}
						for (int i = 0; i < (int) writeImageText.getWidth(); i++) {
							for (int j = 0; j < (int) writeImageText.getHeight(); j++) {
								writeImage.getPixelWriter().setColor(i + (int) writeImageIcon.getWidth(), j, writeImageText.getPixelReader().getColor(i, j));
							}
						}

						if (image == null) {
							image = writeImage;
						} else {
							WritableImage imageTemp = image;
							image = new WritableImage(imageTemp.getWidth() > writeImage.getWidth() ? (int) imageTemp.getWidth() : (int) writeImage.getWidth(), (int) (imageTemp.getHeight() + writeImage.getHeight()));
							imageTemp.getPixelReader().getColor((int) (imageTemp.getWidth() - 1), (int) (imageTemp.getHeight() - 1));
							for (int x = 0; x < (int) imageTemp.getWidth(); x++) {
								for (int y = 0; y < (int) imageTemp.getHeight(); y++) {
									image.getPixelWriter().setColor(x, y, imageTemp.getPixelReader().getColor(x, y));
								}
							}
							for (int i = 0; i < (int) writeImage.getWidth(); i++) {
								for (int j = 0; j < (int) writeImage.getHeight(); j++) {
									image.getPixelWriter().setColor(i, (int) (j + imageTemp.getHeight()), writeImage.getPixelReader().getColor(i, j));
								}
							}
						}
					}

					// WritableImage image = tableTreeView.snapshot(new
					// SnapshotParameters(), null);
					if (image != null) {
						db.setDragView(image);
						/*  将一个字符串放到Dragboard中  */
						ClipboardContent content = new ClipboardContent();
						content.put(dataFormatType, TYPE_DRAG_BEAN);
						content.put(dataFormatTableNames, StringUtil.list2Str(tableNames, ","));
						db.setContent(content);
					}
				}
				event.consume();
			}
		});
		beanTreeView.setOnDragDone(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				/* the drag-and-drop gesture ended */
				System.out.println("onDragDone");
				/* if the data was successfully moved, clear it */
				if (event.getTransferMode() == TransferMode.MOVE) {
					event.getDragboard().clear();
				}

				event.consume();
			}
		});

	}

	@FXML
	public void handCreate() {
		List<TableBean> beans = new ArrayList<TableBean>();
		for (TreeItem<TableBean> itemBean : beanTreeView.getRoot().getChildren()) {
			for (TreeItem<TableBean> item : itemBean.getChildren()) {
				if (StringUtil.isEmptyOrNull(item.getValue().getName())) {
					item.getParent().setExpanded(true);
					DialogUtil.showWarning("生成JavaBean", "JavaBean名称不能为空！");
					return;
				}
				beans.add(item.getValue());
			}
		}
		ObservableList<ForeignColumn> tableItems = tableView.getItems();
		List<ForeignColumn> foreignColumns = new ArrayList<ForeignColumn>();
		for (ForeignColumn item : tableItems) {
			if (item.isChecked()) {
				foreignColumns.add(item);
			}
		}
		if (StringUtil.isEmptyOrNull(packageDirectory.getText())) {
			DialogUtil.showWarning("生成JavaBean", "包目录不能为空！");
			return;
		}
		if (beans.size() == 0) {
			DialogUtil.showWarning("生成JavaBean", "JavaBean不能为空！");
			return;
		}
		if (StringUtil.isEmptyOrNull(url.getText())) {
			DialogUtil.showWarning("数据库连接", "数据库连接地址不能为空！");
			return;
		}
		if (StringUtil.isEmptyOrNull(userName.getText())) {
			DialogUtil.showWarning("数据库连接", "数据库连接用户名不能为空！");
			return;
		}
		if (StringUtil.isEmptyOrNull(password.getText())) {
			DialogUtil.showWarning("数据库连接", "数据库连接密码不能为空！");
			return;
		}

		ConnectionFactory.init(url.getText(), userName.getText(), password.getText());

		try {

			TableFile tableFileTemplet = (TableFile) FileUtil.loadJAXB(TableFile.class, new File(PropertiesUtil.getProperty("lastTableFile")));

			List<TableFile> tableFiles = new ArrayList<TableFile>();

			try (Connection connection = ConnectionFactory.getConnection()) {
				logger.info("开始生成JavaBean");
				DatabaseMetaData databaseMetaData = connection.getMetaData();
				String catalog = connection.getCatalog();

				for (TableBean bean : beans) {
					String tableName = bean.getLinkName();
					logger.info(String.format("开始生成表：%s对应JavaBean", tableName));
					List<DbColumn> columns = new ArrayList<>();
					// 表内的字段的名字和类型
					try (ResultSet resultSet = databaseMetaData.getColumns(null, "%", tableName, "%");) {
						while (resultSet.next()) {
							DbColumn dbColumn = new DbColumn();
							String dataType = resultSet.getString("TYPE_NAME");
							String columnSize = resultSet.getString("COLUMN_SIZE");
							String decimalDigits = resultSet.getString("DECIMAL_DIGITS");
							dbColumn.setColumnName(resultSet.getString("COLUMN_NAME"));
							dbColumn.setDataType(dataType);
							dbColumn.setComment(resultSet.getString("REMARKS"));
							dbColumn.setNullEnable(resultSet.getBoolean("NULLABLE"));
							dbColumn.setArguments(countArguments(dataType, columnSize, decimalDigits));
							columns.add(dbColumn);
						}

					}
					// 表的主键
					try (ResultSet resultSet = databaseMetaData.getPrimaryKeys(catalog, null, tableName)) {
						while (resultSet.next()) {
							String columnName = resultSet.getString("COLUMN_NAME");
							Optional<DbColumn> optColumn = columns.stream().filter(x -> x.getColumnName().equals(columnName)).findFirst();
							if (optColumn.isPresent()) {
								optColumn.get().setPrimaryKey(true);
							}
						}
					}
					TableFile tableFile = new TableFile();
					tableFile.setAnnotationFileNames(tableFileTemplet.getAnnotationFileNames() == null ? new ArrayList<String>() : tableFileTemplet.getAnnotationFileNames());
					List<String> tableImportNames = new ArrayList<String>();
					if (tableFileTemplet.getImportNames() != null) {
						tableImportNames.addAll(tableFileTemplet.getImportNames());
					}
					tableFile.setImportNames(tableImportNames);
					tableFile.setJavaDBs(new ArrayList<JavaDB>());
					tableFile.setComment(bean.getComment());
					tableFile.setTableName(tableName);
					tableFile.setPackageName(String.format("package %s", bean.getParentName()));
					tableFile.setFileName(bean.getName());

					List<String> importNames = new ArrayList<String>();
					Map<String, String> dataTypMap = new HashMap<String, String>();
					for (ForeignColumn foreignColumn : foreignColumns) {
						if (foreignColumn.getTableName().equals(tableName)) {
							String foreignTableName = foreignColumn.getForeignTableName();
							Optional<TableBean> optForeignBean = beans.stream().filter(x -> foreignTableName.equals(x.getLinkName())).findFirst();
							if (optForeignBean.isPresent()) {
								dataTypMap.put(foreignColumn.getColumnName(), optForeignBean.get().getName());
								if (!optForeignBean.get().getParentName().equals(bean.getParentName())) {
									importNames.add(String.format("import %s.%s", optForeignBean.get().getParentName(), optForeignBean.get().getName()));
								}

							}

						}
					}
					tableFile.getImportNames().addAll(importNames);
					for (DbColumn column : columns) {
						System.out.println(column.getColumnName());
						String dataType = dataTypMap.get(column.getColumnName());
						if (dataType != null) {
							JavaDB javaDB = new JavaDB();
							javaDB.setDbColumn(column);
							JavaColumn javaColumn = new JavaColumn();
							javaColumn.setDataType(dataType);
							javaColumn.setColumnName(StringUtil.pascal2Camel(dataType));
							javaColumn.setAnnotationGetNames(new ArrayList<String>());
							javaColumn.setImportNames(new ArrayList<String>());
							javaColumn.getImportNames().add("import javax.persistence.JoinColumn");
							javaColumn.getImportNames().add("import javax.persistence.ManyToOne");
							javaColumn.getImportNames().add("import javax.persistence.FetchType");
							javaColumn.getAnnotationGetNames().add("@ManyToOne(fetch = FetchType.LAZY)");
							javaColumn.getAnnotationGetNames().add("@JoinColumn(name = #cloumnName)");
							javaDB.setJavaColumn(javaColumn);
							tableFile.getJavaDBs().add(javaDB);
						} else {
							List<JavaDB> javaDbTypes = tableFileTemplet.getObJavaDB().stream().filter(x -> FormatSql.equalsDataType(x.getDbColumn(), column)).collect(Collectors.toList());
							List<JavaDB> javaDbPrimaryKeys = javaDbTypes.stream().filter(x -> FormatSql.equalsPrimaryKey(x.getDbColumn(), column)).collect(Collectors.toList());
							List<JavaDB> javaDbNullEnable = javaDbPrimaryKeys.stream().filter(x -> FormatSql.equalsNullEnable(x.getDbColumn(), column)).collect(Collectors.toList());
							List<JavaDB> javaDbArguments = javaDbNullEnable.stream().filter(x -> FormatSql.equalsArguments(x.getDbColumn(), column)).collect(Collectors.toList());
							JavaDB javaDB = null;
							if (!javaDbArguments.isEmpty()) {
								javaDB = javaDbArguments.get(0).clone();
							} else if (!javaDbNullEnable.isEmpty()) {
								javaDB = javaDbNullEnable.get(0).clone();
							} else if (!javaDbPrimaryKeys.isEmpty()) {
								javaDB = javaDbPrimaryKeys.get(0).clone();
							} else if (!javaDbTypes.isEmpty()) {
								javaDB = javaDbTypes.get(0).clone();
							}
							if (javaDB != null) {
								PropertyUtils.copyProperties(javaDB.getDbColumn(), column);
								javaDB.getJavaColumn().setColumnName(StringUtil.underline2Camel(column.getColumnName()));
								tableFile.getJavaDBs().add(javaDB);

							} else {
								// lostDB.append(dbColumn.getColumnName() +
								// "\r\n");
								StringBuilder sb = new StringBuilder();
								if (!StringUtil.isEmptyOrNull(column.getArguments())) {
									sb.append("参数" + column.getArguments());
								}
								if (StringUtil.isBooleanTrue(column.getPrimaryKey())) {
									sb.append(" 是主键");
								}
								if (StringUtil.isBooleanTrue(column.isNullEnable())) {
									sb.append(" 不为空");
								}
								logger.warn(String.format("列名%s 类型%s %s 没有找到对应关系", column.getColumnName(), column.getDataType(), sb.toString()));
							}
						}
					}
					tableFiles.add(tableFile);
				}
			}
			FileUtil.createBean(packageDirectory.getText(), tableFiles);

			PropertiesUtil.writeProperties("dbUrl", url.getText());
			PropertiesUtil.writeProperties("dbUserName", userName.getText());
			PropertiesUtil.writeProperties("packageDirectory", packageDirectory.getText());
			PropertiesUtil.writeProperties("packagePrefix", packagePrefix.getText());
			logger.info("结束生成JavaBean");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@FXML
	public void handChoose() {
		DirectoryChooser chooser = new DirectoryChooser();
		File chosenDir = chooser.showDialog(mainApp.getPrimaryStage());
		if (chosenDir != null) {
			packageDirectory.setText(chosenDir.getAbsolutePath());
		}
	}

	@FXML
	public void handUpdatePackage() {
		if (DialogUtil.showConfirmation("更新包", "更新包会先清除已有的包，确认继续？")) {
			UpdatePackage();
		}
	}

	private void UpdatePackage() {
		List<String> prefixs = StringUtil.str2List(tablePrefix.getText(), ";");
		if (prefixs == null || prefixs.size() == 0) {
			return;
		}
		// prefixs.sort((h1, h2) -> h1.compareTo(h2));
		beanTreeView.getRoot().getChildren().clear();

		// 重置表状态
		for (TreeItem<TableBean> item : tableTreeView.getRoot().getChildren()) {
			item.getValue().setUsed(false);
			item.setGraphic(new ImageView(new Image(tableIcon)));
		}
		for (String prefix : prefixs) {
			TableBean pack = new TableBean(TableBean.TYPE_PACKAGE, String.format("%s.%s.%s", packagePrefix.getText(), StringUtil.underline2Point(prefix), packageSuffix));
			TreeItem<TableBean> itemPack = new TreeItem<TableBean>(pack);
			beanTreeView.getRoot().getChildren().add(itemPack);
		}
		prefixs.sort((h1, h2) -> h2.compareTo(h1));
		for (String prefix : prefixs) {
			TreeItem<TableBean> itemPack = findTableItemByName(beanTreeView.getRoot().getChildren(), String.format("%s.%s.%s", packagePrefix.getText(), StringUtil.underline2Point(prefix), packageSuffix));
			List<TreeItem<TableBean>> list = findTableItemByPrefix(tableTreeView.getRoot().getChildren(), prefix);
			for (TreeItem<TableBean> item : list) {
				if (!item.getValue().isUsed()) {
					String tableName = item.getValue().getName();
					TableBean bean = new TableBean(TableBean.TYPE_BEAN, StringUtil.underline2Pascal(tableName, StringUtil.str2List(tablePrefix.getText(), ";")), tableName, itemPack.getValue().getName());
					TreeItem<TableBean> itemBean = new TreeItem<TableBean>(bean, new ImageView(new Image(beanIcon)));
					itemPack.getChildren().add(itemBean);
					item.getValue().setUsed(true);
					bean.setComment(item.getValue().getComment());
					item.setGraphic(new ImageView(new Image(tableUsedIcon)));
				}
			}
			itemPack.setGraphic(new ImageView(new Image(itemPack.getChildren().size() > 0 ? packageIcon : packageEmptyIcon)));
		}

		tableTreeView.refresh();
		beanTreeView.refresh();
	}

	/**
	 * 连接数据库
	 */
	@FXML
	public void handConnect() {
		if (StringUtil.isEmptyOrNull(url.getText())) {
			DialogUtil.showWarning("数据库连接", "数据库连接地址不能为空！");
			return;
		}
		if (StringUtil.isEmptyOrNull(userName.getText())) {
			DialogUtil.showWarning("数据库连接", "数据库连接用户名不能为空！");
			return;
		}
		if (StringUtil.isEmptyOrNull(password.getText())) {
			DialogUtil.showWarning("数据库连接", "数据库连接密码不能为空！");
			return;
		}

		ConnectionFactory.init(url.getText(), userName.getText(), password.getText());

		try (Connection connection = ConnectionFactory.getConnection()) {
			DatabaseMetaData databaseMetaData = connection.getMetaData();
			String catalog = connection.getCatalog();
			databaseType.setText(String.format("%s数据库", databaseMetaData.getDatabaseProductName()));
			tableTreeView.getRoot().getChildren().clear();
			List<String> tablePrefixList = new ArrayList<String>();
			ObservableList<ForeignColumn> foreignColumns = FXCollections.observableArrayList();

			// 表名、表注释
			try (ResultSet tablesResultSet = databaseMetaData.getTables(catalog, connection.getMetaData().getUserName(), null, new String[] { "TABLE" })) {
				while (tablesResultSet.next()) {

					String tableName = tablesResultSet.getString("TABLE_NAME");
					String comment = tablesResultSet.getString("REMARKS");
					if (tableName.indexOf("_") > -1) {
						String tablePrefixText = tableName.substring(0, tableName.indexOf("_"));
						if (!tablePrefixList.stream().filter(x -> tablePrefixText.equals(x)).findFirst().isPresent()) {
							tablePrefixList.add(tablePrefixText);
						}

					}
					TreeItem<TableBean> item = new TreeItem<>(new TableBean(TableBean.TYPE_TABLE, tableName), new ImageView(new Image(tableIcon)));

					item.getValue().setComment(comment);
					tableTreeView.getRoot().getChildren().add(item);

					// 获取被引用的表，它的主键就是当前表的外键
					try (ResultSet foreignKeyResultSet = databaseMetaData.getImportedKeys(catalog, null, tableName)) {
						while (foreignKeyResultSet.next()) {
							String pkTablenName = foreignKeyResultSet.getString("PKTABLE_NAME");
							String fkColumnName = foreignKeyResultSet.getString("FKCOLUMN_NAME");
							String pkColumnName = foreignKeyResultSet.getString("PKCOLUMN_NAME");
							ForeignColumn foreignColumn = new ForeignColumn(tableName, fkColumnName, pkTablenName, pkColumnName, true);
							foreignColumns.add(foreignColumn);
						}
					}

				}
			}

			tablePrefix.setText(StringUtil.list2Str(tablePrefixList, ";"));
			UpdatePackage();
			tableView.setItems(foreignColumns);
			System.out.println("成功");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(String.format("失败，%s", e.getMessage()));
		}
	}

	/**
	 * 添加bean
	 */
	@FXML
	public void handAdd() {
		ObservableList<TreeItem<TableBean>> tableTreeItemsSelect = tableTreeView.getSelectionModel().getSelectedItems();
		ObservableList<TreeItem<TableBean>> beanTreeItemsSelect = beanTreeView.getSelectionModel().getSelectedItems();
		ObservableList<TreeItem<TableBean>> tableTreeItems = FXCollections.observableArrayList();
		ObservableList<TreeItem<TableBean>> beanTreeItems = FXCollections.observableArrayList();

		for (TreeItem<TableBean> treeItem : tableTreeItemsSelect) {
			if (TableBean.TYPE_TABLE.equals(treeItem.getValue().getType()) && !treeItem.getValue().isUsed()) {
				tableTreeItems.add(treeItem);
			}
		}
		for (TreeItem<TableBean> treeItem : beanTreeItemsSelect) {
			if (TableBean.TYPE_PACKAGE.equals(treeItem.getValue().getType())) {
				beanTreeItems.add(treeItem);
			}
		}
		if (tableTreeItems.size() == 0) {
			DialogUtil.showWarning("选择", "请选择未被使用的表！");
			return;
		}
		if (beanTreeItems.size() == 0) {
			DialogUtil.showWarning("选择", "请选择包！");
			return;
		}
		if (beanTreeItems.size() > 1) {
			DialogUtil.showWarning("选择", "添加时只能选择一个包！");
			return;
		}

		TreeItem<TableBean> item = beanTreeItems.get(0);
		boolean hasChildren = item.getChildren().size() > 0;

		for (TreeItem<TableBean> treeItem : tableTreeItems) {
			TreeItem<TableBean> itemBean = null;
			String tableName = treeItem.getValue().getName();
			TableBean bean = new TableBean(TableBean.TYPE_BEAN, StringUtil.underline2Pascal(tableName, StringUtil.str2List(tablePrefix.getText(), ";")), tableName, item.getValue().getName());
			itemBean = new TreeItem<TableBean>(bean, new ImageView(new Image(beanIcon)));
			item.getChildren().add(itemBean);
			TreeItem<TableBean> itemBeanTable = findTableItemByName(tableTreeView.getRoot().getChildren(), tableName);
			bean.setComment(itemBeanTable.getValue().getComment());
			itemBeanTable.getValue().setUsed(true);
			itemBeanTable.setGraphic(new ImageView(new Image(tableUsedIcon)));
		}
		if (!hasChildren) {
			item.setGraphic(new ImageView(new Image(packageIcon)));
		}
		tableTreeView.refresh();
		item.setExpanded(true);
	}

	/**
	 * 删除包或bean
	 */
	@FXML
	public void handRemove() {

		ObservableList<TreeItem<TableBean>> treeItems = beanTreeView.getSelectionModel().getSelectedItems();
		ObservableList<TreeItem<TableBean>> treeItemBeans = FXCollections.observableArrayList();
		Optional<TreeItem<TableBean>> optTreeItem = treeItems.stream().filter(x -> !TableBean.TYPE_PACKAGE.equals(x.getValue().getType())).findFirst();
		// 选中的是否全是包
		boolean isAllPackage = true;
		isAllPackage = !optTreeItem.isPresent();
		for (TreeItem<TableBean> treeItem : treeItems) {
			if (TableBean.TYPE_BEAN.equals(treeItem.getValue().getType())) {
				treeItemBeans.add(treeItem);
			} else if (TableBean.TYPE_PACKAGE.equals(treeItem.getValue().getType()) && (isAllPackage || !treeItem.isExpanded()) && treeItem.getChildren().size() > 0) {
				treeItemBeans.addAll(treeItem.getChildren());
			} else if (TableBean.TYPE_PACKAGE.equals(treeItem.getValue().getType()) && treeItem.getChildren().size() == 0) {
				treeItemBeans.add(treeItem);
			}
		}
		for (TreeItem<TableBean> treeItem : treeItemBeans) {
			if (TableBean.TYPE_BEAN.equals(treeItem.getValue().getType())) {
				String tableName = treeItem.getValue().getLinkName();
				treeItem = findTableItemByLinkName(beanTreeView.getRoot().getChildren(), tableName);
				TreeItem<TableBean> parent = treeItem.getParent();

				ObservableList<TreeItem<TableBean>> childrens = treeItem.getParent().getChildren();
				childrens.remove(treeItem);
				if (childrens.size() == 0) {
					parent.setGraphic(new ImageView(new Image(packageEmptyIcon)));
				}
				TreeItem<TableBean> itemBeanTable = findTableItemByName(tableTreeView.getRoot().getChildren(), tableName);
				itemBeanTable.getValue().setUsed(false);
				itemBeanTable.setGraphic(new ImageView(new Image(tableIcon)));
			} else if (TableBean.TYPE_PACKAGE.equals(treeItem.getValue().getType())) {
				beanTreeView.getRoot().getChildren().remove(treeItem);
			}
		}
		tableTreeView.refresh();
		beanTreeView.refresh();

	}

	private final class TreeCellImpl extends TreeCell<TableBean> {
		public TreeCellImpl() {

		}

		@Override
		public void updateItem(TableBean item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setText(null);
				setGraphic(null);
				this.setTooltip(null);
			} else {
				setText(getString());
				setGraphic(getTreeItem().getGraphic());
				if (TableBean.TYPE_TABLE.equals(item.getType()) && !StringUtil.isEmptyOrNull(item.getComment())) {
					Tooltip tooltip = new Tooltip();
					tooltip.setText(item.getComment());
					this.setTooltip(tooltip);
				}
			}

		}

		private String getString() {
			return getItem() == null ? "" : getItem().getName();
		}
	}

	private final class TextFieldTreeCellImpl extends TreeCell<TableBean> {

		private TextField textField;
		private TextFieldTreeCellImpl textFieldTreeCellImpl = this;
		private ContextMenu addMenu = new ContextMenu();
		private ContextMenu removeMenu = new ContextMenu();

		public TextFieldTreeCellImpl() {
			nodes.add(this);
			MenuItem addMenuItem = new MenuItem("新建包");
			addMenu.getItems().add(addMenuItem);
			addMenuItem.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent t) {
					TableBean bean = new TableBean(TableBean.TYPE_PACKAGE, String.format("%s.scene%d.%s", packagePrefix.getText(), packgeCount + 1, packageSuffix));
					TreeItem<TableBean> itemBean = new TreeItem<TableBean>(bean, new ImageView(new Image(packageEmptyIcon)));
					getTreeItem().getChildren().add(itemBean);
					packgeCount++;
				}

			});

			MenuItem removeMenuItem = new MenuItem("移除");
			removeMenu.getItems().add(removeMenuItem);
			removeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent t) {
					handRemove();
				}

			});
			this.setOnDragOver(new EventHandler<DragEvent>() {
				@Override
				public void handle(DragEvent event) {
					TableBean item = textFieldTreeCellImpl.getItem();
					if (item != null && TableBean.TYPE_PACKAGE.equals(item.getType()) && event.getDragboard().getContent(dataFormatTableNames) != null) {
						// 允许接收拖动事件
						event.acceptTransferModes(TransferMode.MOVE);
					}

					event.consume();
				}
			});

			this.setOnDragEntered(new EventHandler<DragEvent>() {
				@Override
				public void handle(DragEvent event) {
					TableBean item = textFieldTreeCellImpl.getItem();
					if (item != null && TableBean.TYPE_PACKAGE.equals(item.getType()) && event.getDragboard().getContent(dataFormatTableNames) != null) {
						Optional<TreeCell<TableBean>> optNode = nodes.stream().filter(x -> item.equals(x.getItem())).findFirst();
						if (optNode.isPresent()) {
							// 设置字体倾斜
							optNode.get().setFont(Font.font("Verdana", FontPosture.ITALIC, optNode.get().getFont().getSize() + 1));
						}
					}

					event.consume();
				}
			});

			this.setOnDragExited(new EventHandler<DragEvent>() {
				@Override
				public void handle(DragEvent event) {
					/* mouse moved away, remove the graphical cues */
					TableBean item = textFieldTreeCellImpl.getItem();
					if (item != null && TableBean.TYPE_PACKAGE.equals(item.getType()) && event.getDragboard().getContent(dataFormatTableNames) != null) {
						Optional<TreeCell<TableBean>> optNode = nodes.stream().filter(x -> item.equals(x.getItem())).findFirst();
						if (optNode.isPresent()) {
							// 设置字体正常
							optNode.get().setFont(Font.font(nodes.get(0).getFont().getFamily(), FontPosture.REGULAR, nodes.get(0).getFont().getSize()));
						}
					}

					event.consume();
				}
			});
			this.setOnDragDropped(new EventHandler<DragEvent>() {
				@Override
				public void handle(DragEvent event) {
					/* data dropped */
					System.out.println("onDragDropped");
					/*
					 * if there is a string data on dragboard, read it and use
					 * it
					 */
					Dragboard db = event.getDragboard();
					boolean success = false;
					TableBean item = textFieldTreeCellImpl.getItem();
					boolean hasChildren = textFieldTreeCellImpl.getTreeItem().getChildren().size() > 0;
					System.out.println(db.getContent(dataFormatTableNames));
					if (item != null && TableBean.TYPE_PACKAGE.equals(item.getType()) && db.getContent(dataFormatTableNames) != null) {
						String[] array = db.getContent(dataFormatTableNames).toString().split(",");
						for (String tableName : array) {
							TreeItem<TableBean> itemBean = null;
							if (TYPE_DRAG_TABLE.equals(db.getContent(dataFormatType))) {
								TableBean bean = new TableBean(TableBean.TYPE_BEAN, StringUtil.underline2Pascal(tableName, StringUtil.str2List(tablePrefix.getText(), ";")), tableName, item.getName());
								itemBean = new TreeItem<TableBean>(bean, new ImageView(new Image(beanIcon)));
								TreeItem<TableBean> itemBeanTable = findTableItemByName(tableTreeView.getRoot().getChildren(), tableName);
								itemBeanTable.getValue().setUsed(true);
								itemBean.getValue().setComment(itemBeanTable.getValue().getComment());
								itemBeanTable.setGraphic(new ImageView(new Image(tableUsedIcon)));
							} else if (TYPE_DRAG_BEAN.equals(db.getContent(dataFormatType))) {
								itemBean = findTableItemByLinkName(beanTreeView.getRoot().getChildren(), tableName);
								TreeItem<TableBean> parent = itemBean.getParent();

								ObservableList<TreeItem<TableBean>> childrens = itemBean.getParent().getChildren();
								childrens.remove(itemBean);
								if (parent != textFieldTreeCellImpl.getTreeItem() && childrens.size() == 0) {
									parent.setGraphic(new ImageView(new Image(packageEmptyIcon)));
								}
							}

							textFieldTreeCellImpl.getTreeItem().getChildren().add(itemBean);

						}
						if (!hasChildren) {
							textFieldTreeCellImpl.getTreeItem().setGraphic(new ImageView(new Image(packageIcon)));
						}
						tableTreeView.refresh();
						beanTreeView.refresh();
						textFieldTreeCellImpl.getTreeItem().setExpanded(true);
						success = true;
					}
					/*
					 * let the source know whether the string was successfully
					 * transferred and used
					 */
					event.setDropCompleted(success);

					event.consume();
				}
			});

		}

		@Override
		public void startEdit() {
			if (TableBean.TYPE_PACKAGE_ROOT.equals(this.getItem().getType())) {
				return;
			}
			super.startEdit();

			if (textField == null) {
				createTextField();
			}
			textField.setText(getItem().getName());
			setText(null);
			setGraphic(textField);
			textField.selectAll();
		}

		@Override
		public void cancelEdit() {
			getItem().setName(textField.getText());
			super.cancelEdit();
			setText(getItem().getName());
			setGraphic(getTreeItem().getGraphic());
			for (TreeItem<TableBean> itemBean : this.getTreeItem().getChildren()) {
				itemBean.getValue().setParentName(getText());
			}
			beanTreeView.refresh();
		}

		@Override
		public void updateItem(TableBean item, boolean empty) {
			super.updateItem(item, empty);

			if (empty) {
				setText(null);
				setGraphic(null);
				setContextMenu(null);
				this.setTooltip(null);
			} else {
				if (isEditing()) {
					if (textField != null) {
						textField.setText(getString());
					}
					setText(null);
					setGraphic(textField);
				} else {
					setText(getString());
					setGraphic(getTreeItem().getGraphic());
					if (TableBean.TYPE_PACKAGE_ROOT.equals(item.getType())) {
						setContextMenu(addMenu);
					} else if (TableBean.TYPE_PACKAGE.equals(item.getType()) || TableBean.TYPE_BEAN.equals(item.getType())) {
						setContextMenu(removeMenu);
					}
					if (TableBean.TYPE_BEAN.equals(item.getType()) && !StringUtil.isEmptyOrNull(item.getComment())) {
						Tooltip tooltip = new Tooltip();
						tooltip.setText(item.getComment());
						this.setTooltip(tooltip);
					}

				}
			}
		}

		private void createTextField() {
			textField = new TextField(getString());
			textField.setOnKeyReleased((KeyEvent t) -> {
				if (t.getCode() == KeyCode.ENTER) {
					TableBean item = textFieldTreeCellImpl.getItem();
					item.setName(textField.getText());
					commitEdit(item);
				} else if (t.getCode() == KeyCode.ESCAPE) {
					cancelEdit();
				}
			});
		}

		private String getString() {
			return getItem() == null ? "" : getItem().getName();
		}

	}

	/**
	 * 初始化数据库数据类型默认精度
	 */
	private void initSqlTypeDefeat() {
		HSSFWorkbook workbook = null;
		try {
			sqlTypeDefeats = new ArrayList<SqlTypeDefeat>();
			workbook = new HSSFWorkbook(new FileInputStream(PropertiesUtil.getProperty("sqlTypeDefeatFile")));
			String sheetName = "SQL TYPE DEFEAT";
			for (int i = 1; !StringUtil.isEmptyOrNull(workbook.getSheet(sheetName).getRow(i).getCell(0).getStringCellValue()); i++) {
				SqlTypeDefeat sqlTypeDefeat = new SqlTypeDefeat();
				sqlTypeDefeat.setDataType(workbook.getSheet(sheetName).getRow(i).getCell(0).getStringCellValue());
				sqlTypeDefeat.setDataLength(getCellValueInteger(workbook, sheetName, i, 1));
				sqlTypeDefeat.setDataPrecision(getCellValueInteger(workbook, sheetName, i, 2));
				sqlTypeDefeat.setDataScale(getCellValueInteger(workbook, sheetName, i, 3));
				sqlTypeDefeats.add(sqlTypeDefeat);
			}
		} catch (IOException | ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private Integer getCellValueInteger(HSSFWorkbook workbook, String sheetName, int row, int col) {
		String value = null;
		Cell cell = workbook.getSheet(sheetName).getRow(row).getCell(col);
		switch (cell.getCellType()) { // 根据cell中的类型来输出数据
		case HSSFCell.CELL_TYPE_NUMERIC:
			value = Double.valueOf(cell.getNumericCellValue()).toString();
			break;
		case HSSFCell.CELL_TYPE_STRING:
			value = cell.getStringCellValue();
			break;
		}
		try {
			return (int) Double.parseDouble(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * 根据名称查找树节点
	 *
	 * @param tableItemss
	 * @param name
	 * @return
	 */
	private TreeItem<TableBean> findTableItemByName(ObservableList<TreeItem<TableBean>> tableItems, String name) {
		Optional<TreeItem<TableBean>> optTableItem = tableItems.stream().filter(x -> x.getValue().getName().equals(name)).findFirst();
		if (optTableItem.isPresent()) {
			return optTableItem.get();
		} else {
			return null;
		}
	}

	/**
	 * 根据前缀查找树节点
	 *
	 * @param tableItems
	 * @param prefix
	 * @return
	 */
	private List<TreeItem<TableBean>> findTableItemByPrefix(ObservableList<TreeItem<TableBean>> tableItems, String prefix) {
		List<TreeItem<TableBean>> treeItems = new ArrayList<TreeItem<TableBean>>();
		for (TreeItem<TableBean> tableItem : tableItems) {
			if (tableItem.getValue().getName().startsWith(prefix)) {
				treeItems.add(tableItem);
			}
		}
		return treeItems;
	}

	/**
	 * 根据关联名称查找树节点
	 *
	 * @param tableItems
	 * @param linkName
	 * @return
	 */
	private TreeItem<TableBean> findTableItemByLinkName(ObservableList<TreeItem<TableBean>> tableItems, String linkName) {
		for (int i = 0; i < tableItems.size(); i++) {
			TreeItem<TableBean> tableItem = tableItems.get(i);
			if (linkName.equals(tableItem.getValue().getLinkName())) {
				return tableItem;
			} else if (tableItem.getChildren().size() > 0) {
				TreeItem<TableBean> tableItemChildren = findTableItemByLinkName(tableItem.getChildren(), linkName);
				if (tableItemChildren != null) {
					return tableItemChildren;
				}
			}
		}
		return null;

	}

	private String countArguments(String dataType, String columnSize, String decimalDigits) {
		List<String> arguments = new ArrayList<String>();

		if (!StringUtil.isEmptyOrNull(columnSize)) {
			arguments.add(columnSize);
		}

		if (!StringUtil.isEmptyOrNull(decimalDigits) && !"0".equals(decimalDigits)) {
			arguments.add(decimalDigits);
		}

		return StringUtil.list2Str(arguments, ",");

	}
}