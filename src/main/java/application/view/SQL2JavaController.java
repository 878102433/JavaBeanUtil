package application.view;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.JAXBException;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.jboss.forge.roaster.Roaster;

import application.model.TableFile;
import application.util.DialogUtil;
import application.util.FileUtil;
import application.util.FormatSql;
import application.util.JavaBuild;
import application.util.PropertiesUtil;
import application.util.StringUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class SQL2JavaController {
	@FXML
	private TextArea sqlTextArea;
	@FXML
	private TextArea javaTextArea;
	@FXML
	private ChoiceBox<String> dbChoiceBox;

	private Stage dialogStage;

	/**
	 * fxml加载后自动调用
	 */
	@FXML
	private void initialize() {
		dbChoiceBox.setItems(FXCollections.observableArrayList(FormatSql.DB_TYPE_ORACLE, FormatSql.DB_TYPE_MYSQL));
		dbChoiceBox.setValue("Oracle");
		sqlTextArea.setText(FormatSql.format("CREATE TABLE BUSS_INDEX_DATA(ID VARCHAR2(50) NOT NULL,INDEX_ID VARCHAR2(50) NOT NULL,DIM_REF_ID  VARCHAR2(50) NOT NULL,CYCLE_ID VARCHAR2(50) NOT NULL,DATA_DATE DATE,DIM_VALUE_1 VARCHAR2(50),DIM_VALUE_2 VARCHAR2(50),DIM_VALUE_3 VARCHAR2(50),DIM_VALUE_4 VARCHAR2(50),INDEX_VALUE NUMBER(12, 3))"));
	}

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	/**
	 * 格式化SQL
	 */
	@FXML
	private void handleSQLFormat() {
		sqlTextArea.setText(FormatSql.format(sqlTextArea.getText()));
	}

	/**
	 * 生成Java代码
	 */
	@FXML
	private boolean handleCreateJava() {
		handleSQLFormat();
		String fileName = null;
		try {
			fileName = PropertiesUtil.getProperty("lastTableFile");
		} catch (ConfigurationException e) {
			DialogUtil.showException("读取配置文件出错", "读取配置文件出错，以下是具体异常：", e);
			return false;
		}
		if (!StringUtil.isEmptyOrNull(fileName)) {

			FormatSql formatSql = null;
			try {
				File file = new File(fileName);
				formatSql = new FormatSql((TableFile) FileUtil.loadJAXB(TableFile.class, file), dbChoiceBox.getValue());
			} catch (JAXBException e) {
				DialogUtil.showException("读取模板文件出错", "读取模板文件出错，以下是具体异常：", e);
				return false;
			}

			TableFile formateTableFile = null;
			StringBuilder sb = null;
			StringBuilder lostDB = null;
			try {
				formatSql.createTableFile(sqlTextArea.getText());
				formateTableFile = formatSql.getTableFile();
				sb = JavaBuild.create(formateTableFile);
				javaTextArea.setText(sb.toString());
				lostDB = formatSql.getLostDB();
			} catch (RuntimeException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				DialogUtil.showException("生成Java代码出错", "生成Java代码出错，以下是具体异常：", e);
				e.printStackTrace();
				return false;
			}

			try {
				fileName = formateTableFile.getFileName();
				// 格式化java代码
				javaTextArea.setText(Roaster.format(sb.toString()));
			} catch (Exception e) {
				DialogUtil.showException("格式化Java代码出错", "格式化Java代码出错，以下是具体异常：", e);
				return false;
			}

			if (lostDB != null && lostDB.length() > 0) {
				DialogUtil.showWarning("生成Java", "以下字段被忽略。因为没找到对应关系。\r\n\r\n" + lostDB.toString());
			}
			return true;

		}
		return false;

	}

	/**
	 * 保存Java代码
	 *
	 * @throws IOException
	 */
	@FXML
	private void handleSaveJava() throws IOException {
		if (handleCreateJava()) {
			FileChooser fileChooser = new FileChooser();

			// Set extension filter
			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JAVA files (*.java)", "*.java");
			fileChooser.getExtensionFilters().add(extFilter);

			// Show save file dialog
			File file = fileChooser.showSaveDialog(dialogStage);

			if (file != null) {
				// Make sure it has the correct extension
				if (!file.getPath().endsWith(".java")) {
					file = new File(file.getPath() + ".java");
				}
				FileUtil.saveFile(javaTextArea.getText(), file);

			}
		}
	}
}
