package application;

import java.io.IOException;

import org.apache.commons.configuration2.ex.ConfigurationException;

import application.model.TableFile;
import application.util.PropertiesUtil;
import application.util.StringUtil;
import application.view.DatabaseController;
import application.view.RootLayoutController;
import application.view.SQL2JavaController;
import application.view.TableFileConfigController;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainApp extends Application {
	private Stage primaryStage;
	private BorderPane rootLayout;

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		initRootLayout();
		showSQL2Java();
	}

	public void initRootLayout() {
		try {
			// Load root layout from fxml file.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
			rootLayout = (BorderPane) loader.load();

			// Show the scene containing the root layout.
			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);
			loadStagePosition();

			this.primaryStage.setTitle("JavaBeanUtil");
			this.primaryStage.getIcons().add(new Image("file:resources/images/JavaBeanUtil.png"));
			// Give the controller access to the main app.
			RootLayoutController controller = loader.getController();
			controller.setMainApp(this);
			primaryStage.widthProperty().addListener(e -> saveStagePosition(e));
			primaryStage.heightProperty().addListener(e -> saveStagePosition(e));
			primaryStage.xProperty().addListener(e -> saveStagePosition(e));
			primaryStage.yProperty().addListener(e -> saveStagePosition(e));
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 从配置文件中读取窗口大小，位置
	 */
	public void loadStagePosition() {

		try {

			String width = PropertiesUtil.getProperty("width");
			String height = PropertiesUtil.getProperty("height");
			String positionX = PropertiesUtil.getProperty("x");
			String positionY = PropertiesUtil.getProperty("y");

			if (!StringUtil.isEmptyOrNull(width)) {
				primaryStage.setWidth(Double.parseDouble(width));
			}
			if (!StringUtil.isEmptyOrNull(height)) {
				primaryStage.setHeight(Double.parseDouble(height));
			}
			if (!StringUtil.isEmptyOrNull(positionX)) {
				primaryStage.setX(Double.parseDouble(positionX));
			}
			if (!StringUtil.isEmptyOrNull(positionY)) {
				primaryStage.setY(Double.parseDouble(positionY));
			}

		} catch (ConfigurationException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 将读取窗口大小，位置写入到配置文件中
	 */
	public void saveStagePosition(Observable o) {
		if (o instanceof ReadOnlyProperty) {
			ReadOnlyProperty<?> property = (ReadOnlyProperty<?>) o;
			try {
				if (!primaryStage.isMaximized()) {
					PropertiesUtil.writeProperties(property.getName(), property.getValue().toString());
				}
			} catch (ConfigurationException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * 加载QL2Java至主界面
	 */
	public void showSQL2Java() {
		try {

			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/Database.fxml"));
			AnchorPane page = (AnchorPane) loader.load();
			DatabaseController controller = loader.getController();
			rootLayout.setCenter(page);
			controller.setMainApp(this);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void showTemplateConfigDialog(TableFile tableFile, String fileName) {
		try {
			// Load the fxml file and create a new stage for the popup dialog.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/TableFileConfig.fxml"));
			AnchorPane page = (AnchorPane) loader.load();

			// Create the dialog Stage.
			Stage dialogStage = new Stage();
			dialogStage.setX(primaryStage.getX() + (primaryStage.getWidth() - page.getPrefWidth()) / 2);
			dialogStage.setY(primaryStage.getY() + (primaryStage.getHeight() - page.getPrefHeight()) / 2);
			dialogStage.getHeight();
			dialogStage.setTitle(String.format("编辑模板（%s）", fileName));
			dialogStage.initModality(Modality.APPLICATION_MODAL);
			dialogStage.initOwner(primaryStage);
			Scene scene = new Scene(page);
			dialogStage.setScene(scene);

			// Set the person into the controller.
			TableFileConfigController controller = loader.getController();
			controller.setDialogStage(dialogStage);
			controller.setTableFile(tableFile);
			// Show the dialog and wait until the user closes it
			dialogStage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();

		}
	}

	public void showSQLDialog() {
		try {
			// Load the fxml file and create a new stage for the popup dialog.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/SQL2Java.fxml"));
			AnchorPane page = (AnchorPane) loader.load();

			// Create the dialog Stage.
			Stage dialogStage = new Stage();
			dialogStage.setX(primaryStage.getX() + (primaryStage.getWidth() - page.getPrefWidth()) / 2);
			dialogStage.setY(primaryStage.getY() + (primaryStage.getHeight() - page.getPrefHeight()) / 2);
			dialogStage.getHeight();
			dialogStage.setTitle("SQL转JavaBean");
			dialogStage.initModality(Modality.NONE);
			dialogStage.initOwner(primaryStage);
			Scene scene = new Scene(page);
			dialogStage.setScene(scene);

			// Set the person into the controller.
			SQL2JavaController controller = loader.getController();
			controller.setDialogStage(dialogStage);

			// Show the dialog and wait until the user closes it
			dialogStage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();

		}
	}

	public static void main(String[] args) {
		launch(args);
	}

	public Stage getPrimaryStage() {
		return primaryStage;
	}

}
