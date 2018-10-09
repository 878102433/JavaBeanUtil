package application.view;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.configuration2.ex.ConfigurationException;

import application.MainApp;
import application.model.JavaDB;
import application.model.TableFile;
import application.util.DialogUtil;
import application.util.FileUtil;
import application.util.PropertiesUtil;
import application.util.StringUtil;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TableFileConfigController {
	@FXML
	private TextField packageTextField;
	@FXML
	private TextArea importTextArea;
	@FXML
	private TextArea annotationTextArea;
	@FXML
	private TableView<JavaDB> javaDBTable;
	@FXML
	private TableColumn<JavaDB, String> dataTypeColumn;
	@FXML
	private TableColumn<JavaDB, String> argumentsColumn;
	@FXML
	private TableColumn<JavaDB, String> primaryKeyColumn;
	@FXML
	private TableColumn<JavaDB, String> nullEnableColumn;
	@FXML
	private TableColumn<JavaDB, String> javaTypeColumn;

	private Stage dialogStage;
	private TableFile tableFile;
	private ObservableList<JavaDB> obJavaDBs;

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	@FXML
	private void initialize() {
		// Initialize the javaDB table with the two columns.
		dataTypeColumn.setCellValueFactory(cellData -> cellData.getValue().dataTypeProperty());
		argumentsColumn.setCellValueFactory(cellData -> cellData.getValue().argumentsProperty());
		primaryKeyColumn.setCellValueFactory(cellData -> cellData.getValue().primaryKeyProperty());
		nullEnableColumn.setCellValueFactory(cellData -> cellData.getValue().nullEnableProperty());
		javaTypeColumn.setCellValueFactory(cellData -> cellData.getValue().javaTypeProperty());

	}

	public void setTableFile(TableFile tableFile) {
		this.tableFile = tableFile;
		packageTextField.setText(tableFile.getPackageName());
		importTextArea.setText(StringUtil.list2Str(tableFile.getImportNames(), "\n"));
		annotationTextArea.setText(StringUtil.list2Str(tableFile.getAnnotationFileNames(), "\n"));
		obJavaDBs = tableFile.getObJavaDB();
		javaDBTable.setItems(obJavaDBs);
	}

	public boolean showJavaDBEditDialog(JavaDB tempJavaDB) {
		try {
			// Load the fxml file and create a new stage for the popup dialog.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/JavaDBConfig.fxml"));
			AnchorPane page = (AnchorPane) loader.load();

			// Create the dialog Stage.
			Stage dialogStage = new Stage();
			dialogStage.setTitle(String.format("SQL、JAVA字段对应关系"));
			dialogStage.initModality(Modality.APPLICATION_MODAL);
			dialogStage.initOwner(this.dialogStage);
			Scene scene = new Scene(page);
			dialogStage.setScene(scene);
			dialogStage.setX(this.dialogStage.getX() + (this.dialogStage.getWidth() - page.getPrefWidth()) / 2);
			dialogStage.setY(this.dialogStage.getY() + (this.dialogStage.getHeight() - page.getPrefHeight()) / 2);
			// Set the person into the controller.
			JavaDBConfigController controller = loader.getController();
			controller.setDialogStage(dialogStage);
			controller.setJavaDB(tempJavaDB);
			// Show the dialog and wait until the user closes it
			dialogStage.showAndWait();
			return controller.isOkClicked();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 重置模板
	 */
	@FXML
	private void handleRestTemplate() {
		try {
			String defaultTableFile = PropertiesUtil.getProperty("defaultTableFile");
			String simpleTableFile = PropertiesUtil.getProperty("simpleTableFile");
			if (!StringUtil.isEmptyOrNull(defaultTableFile) && !StringUtil.isEmptyOrNull(simpleTableFile)) {
				FileUtil.copyFile(defaultTableFile, simpleTableFile);
				PropertiesUtil.writeProperties("lastTableFile", simpleTableFile);
				File file = new File(simpleTableFile);
				setTableFile((TableFile) FileUtil.loadJAXB(TableFile.class, file));
				dialogStage.setTitle(String.format("编辑模板（%s）", file.getName()));
			}
		} catch (JAXBException | IOException | ConfigurationException e) {
			DialogUtil.showException("重置模板", "重置模板失败，以下是具体异常：", e);

		}
	}

	private boolean saveData() {
		if (obJavaDBs != null && obJavaDBs.size() > 0) {
			List<JavaDB> javaDBs = new ArrayList<JavaDB>();
			for (JavaDB javaDB : obJavaDBs) {
				javaDBs.add(javaDB);
			}
			tableFile.setJavaDBs(javaDBs);
			tableFile.setPackageName(StringUtil.empty2Null(packageTextField.getText()));
			tableFile.setImportNames(StringUtil.str2List(importTextArea.getText(), "\n"));
			tableFile.setAnnotationFileNames(StringUtil.str2List(annotationTextArea.getText(), "\n"));
			return true;
		} else {
			DialogUtil.showError("数据校验", "类型对应关系不能为空！");
			return false;
		}

	}

	/**
	 * 保存
	 *
	 * @throws JAXBException
	 */
	@FXML
	private void handleSave() throws JAXBException {
		if (saveData()) {
			try {
				FileUtil.saveJAXB(TableFile.class, tableFile, new File(PropertiesUtil.getProperty("lastTableFile")));
			} catch (ConfigurationException e) {
				DialogUtil.showException("保存", "保存失败，以下是具体异常：", e);
			}
		}

	}

	/**
	 * 另存为
	 *
	 * @throws JAXBException
	 * @throws ConfigurationException
	 */
	@FXML
	private void handleSaveAs() throws JAXBException, ConfigurationException {
		FileChooser fileChooser = new FileChooser();

		// Set extension filter
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
		fileChooser.getExtensionFilters().add(extFilter);
		String fileName = PropertiesUtil.getProperty("lastTableFile");
		if (!StringUtil.isEmptyOrNull(fileName)) {
			File file = new File(fileName);
			fileChooser.setInitialDirectory(file.getParentFile());
		}
		// Show save file dialog
		File file = fileChooser.showSaveDialog(dialogStage);

		if (file != null) {
			// Make sure it has the correct extension
			if (!file.getPath().endsWith(".xml")) {
				file = new File(file.getPath() + ".xml");
			}
			if (saveData()) {
				FileUtil.saveJAXB(TableFile.class, tableFile, file);
			}

		}
	}

	/**
	 * Called when the user clicks the new button. Opens af dialog to edit details
	 * for a new javaDB.
	 */
	@FXML
	private void handleNewJavaDB() {
		JavaDB tempJavaDB = new JavaDB();
		boolean okClicked = showJavaDBEditDialog(tempJavaDB);
		if (okClicked) {
			obJavaDBs.add(tempJavaDB);
			javaDBTable.setItems(obJavaDBs);
		}
	}

	/**
	 * Called when the user clicks the edit button. Opens a dialog to edit details
	 * for the selected javaDB.
	 */
	@FXML
	private void handleEditJavaDB() {
		JavaDB selectedJavaDB = javaDBTable.getSelectionModel().getSelectedItem();
		if (selectedJavaDB != null) {
			boolean okClicked = showJavaDBEditDialog(selectedJavaDB);
			if (okClicked) {
				javaDBTable.refresh();
			}

		} else {
			DialogUtil.showWarning("没有选中", "请选中一行数据！");

		}
	}

	@FXML
	private void handleTableDoubleClick(MouseEvent t) {
		if (t.getClickCount() == 2) {
			JavaDB selectedJavaDB = javaDBTable.getSelectionModel().getSelectedItem();
			if (selectedJavaDB != null) {
				boolean okClicked = showJavaDBEditDialog(selectedJavaDB);
				if (okClicked) {
					javaDBTable.refresh();
				}

			} else {
				DialogUtil.showWarning("没有选中", "请选中一行数据！");
			}
		}
	}

	/**
	 * Called when the user clicks on the delete button.
	 */
	@FXML
	private void handleDeleteJavaDB() {
		int selectedIndex = javaDBTable.getSelectionModel().getSelectedIndex();
		if (selectedIndex >= 0) {
			obJavaDBs.remove(selectedIndex);
			javaDBTable.setItems(obJavaDBs);
		} else {
			DialogUtil.showWarning("没有选中", "请选中一行数据！");
		}
	}

	/**
	 * Called when the user clicks cancel.
	 */
	@FXML
	private void handleCancel() {
		dialogStage.close();
	}

	/**
	 * Called when the user clicks on the move up button.
	 */
	@FXML
	private void handleMoveUpJavaDB() {
		int selectedIndex = javaDBTable.getSelectionModel().getSelectedIndex();
		if (selectedIndex < 0) {
			DialogUtil.showWarning("没有选中", "请选中一行数据！");
			return;
		}
		if (selectedIndex == 0) {
			DialogUtil.showWarning("无法移动", "无法上移，已经是第一条数据！");
		} else {
			JavaDB temp = obJavaDBs.get(selectedIndex);
			obJavaDBs.set(selectedIndex, obJavaDBs.get(selectedIndex - 1));
			obJavaDBs.set(selectedIndex - 1, temp);
			obJavaDBs.remove(selectedIndex);
			javaDBTable.refresh();
			javaDBTable.getSelectionModel().select(selectedIndex - 1);
		}
	}

	/**
	 * Called when the user clicks on the move down button.
	 */
	@FXML
	private void handleMoveDownJavaDB() {
		int selectedIndex = javaDBTable.getSelectionModel().getSelectedIndex();
		if (selectedIndex < 0) {
			DialogUtil.showWarning("没有选中", "请选中一行数据！");
			return;
		}
		if (selectedIndex == obJavaDBs.size() - 1) {
			DialogUtil.showWarning("无法移动", "无法下移，已经是最后一条数据！");
		} else {
			JavaDB temp = obJavaDBs.get(selectedIndex);
			obJavaDBs.set(selectedIndex, obJavaDBs.get(selectedIndex + 1));
			obJavaDBs.set(selectedIndex + 1, temp);
			obJavaDBs.remove(selectedIndex);
			javaDBTable.refresh();
			javaDBTable.getSelectionModel().select(selectedIndex + 1);

		}
	}

	/**
	 * Called when the user clicks the copy button. Opens a dialog to copy details
	 * for the selected javaDB.
	 */
	@FXML
	private void handleCopyJavaDB() {
		JavaDB selectedJavaDB = javaDBTable.getSelectionModel().getSelectedItem();
		int selectedIndex = javaDBTable.getSelectionModel().getSelectedIndex();
		if (selectedJavaDB != null) {
			JavaDB JavaDBClone = selectedJavaDB.clone();
			boolean okClicked = showJavaDBEditDialog(JavaDBClone);
			if (okClicked) {
				obJavaDBs.add(selectedIndex + 1, JavaDBClone);
				javaDBTable.refresh();
				javaDBTable.getSelectionModel().select(selectedIndex + 1);
			}

		} else {
			DialogUtil.showWarning("没有选中", "请选中一行数据！");

		}
	}
}
