package application.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class DialogUtil {
	/**
	 * 对话框工具类
	 */
	public DialogUtil() {

	}

	/**
	 * 消息对话框
	 *
	 * @param title
	 * @param head
	 * @param content
	 */
	public static void showInformation(String title, String head, String content) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(head);
		alert.setContentText(content);
		alert.showAndWait();
	}

	/**
	 * 消息对话框
	 *
	 * @param head
	 * @param content
	 */
	public static void showInformation(String head, String content) {
		showInformation(null, head, content);
	}

	/**
	 * 告警对话框
	 *
	 * @param title
	 * @param head
	 * @param content
	 */
	public static void showWarning(String title, String head, String content) {
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle(title);
		alert.setHeaderText(head);
		alert.setContentText(content);
		alert.showAndWait();
	}

	/**
	 * 告警对话框
	 *
	 * @param head
	 * @param content
	 */
	public static void showWarning(String head, String content) {
		showWarning(null, head, content);
	}

	/**
	 * 错误对话框
	 *
	 * @param title
	 * @param head
	 * @param content
	 */
	public static void showError(String title, String head, String content) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(head);
		alert.setContentText(content);
		alert.showAndWait();
	}

	/**
	 * 错误对话框
	 *
	 * @param head
	 * @param content
	 */
	public static void showError(String head, String content) {
		showError(null, head, content);
	}

	/**
	 * 异常对话框
	 *
	 * @param title
	 * @param head
	 * @param content
	 * @param ex
	 */
	public static void showException(String title, String head, String content, Exception ex) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(head);
		alert.setContentText(content);
		// Create expandable Exception.
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		String exceptionText = sw.toString();

		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(textArea, 0, 1);

		// Set expandable Exception into the dialog pane.
		alert.getDialogPane().setExpandableContent(expContent);

		alert.showAndWait();
	}

	/**
	 * 异常对话框
	 *
	 * @param head
	 * @param content
	 * @param ex
	 */
	public static void showException(String head, String content, Exception ex) {
		showException(null, head, content, ex);
	}

	/**
	 * 确认对话框
	 *
	 * @param title
	 * @param head
	 * @param content
	 */
	public static boolean showConfirmation(String title, String head, String content) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(title);
		alert.setHeaderText(head);
		alert.setContentText(content);
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 确认对话框
	 *
	 * @param head
	 * @param content
	 */
	public static boolean showConfirmation(String head, String content) {
		return showConfirmation(null, head, content);
	}
}
