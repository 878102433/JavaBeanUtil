package application.view;

import application.model.DbColumn;
import application.model.JavaColumn;
import application.model.JavaDB;
import application.util.DialogUtil;
import application.util.StringUtil;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class JavaDBConfigController {
	@FXML
	private TextField dataTypeTextField;
	@FXML
	private TextField argumentsTextField;
	@FXML
	private CheckBox primaryKeyCheckBox;
	@FXML
	private CheckBox nullEnableCheckBox;
	@FXML
	private TextField javaTypeTextField;
	@FXML
	private TextArea importTextArea;
	@FXML
	private TextArea annotationGetTextArea;
	@FXML
	private TextArea annotationSetTextArea;

	private Stage dialogStage;
	private JavaDB javaDB;
	private boolean okClicked = false;

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	@FXML
	private void initialize() {
	}

	public void setJavaDB(JavaDB javaDB) {
		this.javaDB = javaDB;
		if (javaDB.getDbColumn() != null) {
			dataTypeTextField.setText(javaDB.getDbColumn().getDataType());
			argumentsTextField.setText(javaDB.getDbColumn().getArguments());
			primaryKeyCheckBox.setSelected(StringUtil.isBooleanTrue(javaDB.getDbColumn().getPrimaryKey()));
			nullEnableCheckBox.setSelected(StringUtil.isBooleanTrue(javaDB.getDbColumn().getNullEnable()));
		}
		if (javaDB.getJavaColumn() != null) {
			javaTypeTextField.setText(javaDB.getJavaColumn().getDataType());
			importTextArea.setText(StringUtil.list2Str(javaDB.getJavaColumn().getImportNames(), "\n"));
			annotationGetTextArea.setText(StringUtil.list2Str(javaDB.getJavaColumn().getAnnotationGetNames(), "\n"));
			annotationSetTextArea.setText(StringUtil.list2Str(javaDB.getJavaColumn().getAnnotationSetNames(), "\n"));
		} else {
			importTextArea.setText("import javax.persistence.Column;");
			annotationGetTextArea.setText("@Column(name = #cloumnName)");
		}

	}

	public boolean isOkClicked() {
		return okClicked;
	}

	/**
	 * Called when the user clicks ok.
	 */
	@FXML
	private void handleOk() {
		if (isInputValid()) {
			if (javaDB.getDbColumn() == null) {
				javaDB.setDbColumn(new DbColumn());
			}
			if (javaDB.getJavaColumn() == null) {
				javaDB.setJavaColumn(new JavaColumn());
			}

			javaDB.getDbColumn().setDataType(dataTypeTextField.getText());
			javaDB.getDbColumn().setArguments(StringUtil.empty2Null(argumentsTextField.getText()));
			javaDB.getDbColumn().setPrimaryKey(primaryKeyCheckBox.isSelected() ? true : null);
			javaDB.getDbColumn().setNullEnable(nullEnableCheckBox.isSelected() ? true : null);

			javaDB.getJavaColumn().setDataType(javaTypeTextField.getText());
			javaDB.getJavaColumn().setImportNames(StringUtil.str2List(importTextArea.getText(), "\n"));
			javaDB.getJavaColumn().setAnnotationGetNames(StringUtil.str2List(annotationGetTextArea.getText(), "\n"));
			javaDB.getJavaColumn().setAnnotationSetNames(StringUtil.str2List(annotationSetTextArea.getText(), "\n"));
			okClicked = true;
			dialogStage.close();
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
	 * Validates the user input in the text fields.
	 *
	 * @return true if the input is valid
	 */
	private boolean isInputValid() {
		String errorMessage = "";
		if (dataTypeTextField.getText().length() == 0) {
			errorMessage += "SQL字段类型不能为空!\r\n";
		}
		if (javaTypeTextField.getText().length() == 0) {
			errorMessage += "Java字段类型不能为空!\r\n";
		}
		if (errorMessage.length() == 0) {
			return true;
		} else {
			DialogUtil.showError("数据校验", errorMessage);
			return false;
		}
	}
}
