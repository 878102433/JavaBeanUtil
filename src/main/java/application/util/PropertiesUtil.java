package application.util;

import java.time.LocalDateTime;

import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 *
 * PropertiesUtil.java
 *
 * @desc properties 资源文件解析工具
 *
 */
public class PropertiesUtil {

	private static FileBasedConfigurationBuilder<FileBasedConfiguration> builder;
	private final static String fileName = "resources/config/config.properties";

	private static void readProperties() {
		Parameters params = new Parameters();
		builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class).configure(params.properties().setFileName(fileName).setEncoding("UTF-8"));
	}

	/**
	 * 获取某个属性
	 *
	 * @throws ConfigurationException
	 *
	 */
	public static String getProperty(String key) throws ConfigurationException {
		if (builder == null) {
			readProperties();
		}

		return builder.getConfiguration().getString(key);
	}

	/**
	 * 写入properties信息
	 *
	 * @throws ConfigurationException
	 *
	 */
	public static void writeProperties(String key, String value) throws ConfigurationException {

		builder.getConfiguration().setProperty(key, value);
		PropertiesConfiguration propertiesConfiguration = (PropertiesConfiguration) builder.getConfiguration();
		propertiesConfiguration.getLayout().setHeaderComment(LocalDateTime.now().toString());
		builder.save();

	}

}