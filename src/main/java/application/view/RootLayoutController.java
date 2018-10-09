package application.view;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.commons.configuration2.ex.ConfigurationException;

import application.MainApp;
import application.model.TableFile;
import application.util.DialogUtil;
import application.util.FileUtil;
import application.util.PropertiesUtil;
import application.util.StringUtil;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;

/**
 * The controller for the root layout. The root layout provides the basic
 * application layout containing a menu bar and space where other JavaFX
 * elements can be placed.
 *
 * @author Marco Jakob
 */
public class RootLayoutController {

	// Reference to the main application
	private MainApp mainApp;

	/**
	 * 打开模板
	 * 
	 * @throws ConfigurationException
	 * @throws JAXBException
	 */
	@FXML
	private void handleOpenTemplate() throws ConfigurationException, JAXBException {
		FileChooser fileChooser = new FileChooser();

		// Set extension filter
		String fileName = PropertiesUtil.getProperty("lastTableFile");
		if (!StringUtil.isEmptyOrNull(fileName)) {
			File file = new File(fileName);
			fileChooser.setInitialDirectory(file.getParentFile());
		}
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
		fileChooser.getExtensionFilters().add(extFilter);

		// Show save file dialog
		File file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());

		if (file != null) {
			try {
				TableFile tableFile = (TableFile) FileUtil.loadJAXB(TableFile.class, file);
				String templateDirectory = PropertiesUtil.getProperty("templateDirectory");
				if (!StringUtil.isEmptyOrNull(templateDirectory)) {
					FileUtil.copyFile(file.getPath(), templateDirectory + file.getName());
					PropertiesUtil.writeProperties("lastTableFile", templateDirectory + file.getName());
				}
				mainApp.showTemplateConfigDialog(tableFile, file.getName());

			} catch (JAXBException | ConfigurationException | IOException e) {
				DialogUtil.showException("打开模板", "打开模板失败，以下是具体异常：", e);
			}
		}
	}

	/**
	 * 设置模板
	 */
	@FXML
	private void handleConfigTemplate() {
		try {
			String fileName = PropertiesUtil.getProperty("lastTableFile");
			if (!StringUtil.isEmptyOrNull(fileName)) {

				File file = new File(fileName);
				mainApp.showTemplateConfigDialog((TableFile) FileUtil.loadJAXB(TableFile.class, file), file.getName());
			}
		} catch (JAXBException | ConfigurationException e) {
			DialogUtil.showException("设置模板", "设置模板失败，以下是具体异常：", e);
		}

	}

	/**
	 * SQL转javaBean
	 */
	@FXML
	private void handleSQLBuilder() {
		mainApp.showSQLDialog();
	}

	/**
	 * 退出
	 */
	@FXML
	private void handleExit() {
		System.exit(0);
	}

	/**
	 * 关于
	 */
	@FXML
	private void handleAbout() {
		StringBuilder sb = new StringBuilder();
		sb.append("version: 1.0\r\n\r\n");
		sb.append("采用javafx开发，用到主要技术：\r\n");
		sb.append("SQL格式化、解析：druid.jar\r\n");
		sb.append("JAVA格式化：roaster.jar\r\n");
		DialogUtil.showInformation("关于", sb.toString());
	}

	public MainApp getMainApp() {
		return mainApp;
	}

	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}
}