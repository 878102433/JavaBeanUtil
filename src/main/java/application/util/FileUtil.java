package application.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jboss.forge.roaster.Roaster;

import application.model.TableFile;

public class FileUtil {
	/**
	 * JavaBean保存为xml
	 *
	 * @param objectClass
	 * @param object
	 * @param file
	 * @throws JAXBException
	 */
	public static void saveJAXB(Class<?> objectClass, Object object, File file) throws JAXBException {

		JAXBContext context = JAXBContext.newInstance(objectClass);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(object, file);

	}

	/**
	 * 从xml中读取JavaBean
	 *
	 * @param objectClass
	 * @param file
	 * @return
	 * @throws JAXBException
	 */
	public static Object loadJAXB(Class<?> objectClass, File file) throws JAXBException {

		JAXBContext context = JAXBContext.newInstance(objectClass);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		return unmarshaller.unmarshal(file);

	}

	/**
	 * 复制文件
	 *
	 * @param src
	 *            源
	 * @param dest
	 *            目标
	 * @throws IOException
	 */
	public static void copyFile(String src, String dest) throws IOException {

		FileInputStream in = new FileInputStream(src);
		File file = new File(dest);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream out = new FileOutputStream(file);
		int c;
		byte buffer[] = new byte[1024];
		while ((c = in.read(buffer)) != -1) {
			for (int i = 0; i < c; i++)
				out.write(buffer[i]);
		}
		in.close();
		out.close();

	}

	/**
	 * 保存文件
	 *
	 * @param string
	 * @param file
	 * @throws IOException
	 */
	public static void saveFile(String string, File file) throws IOException {
		if (!file.exists()) {
			createFile(file);
		}
		FileOutputStream out = new FileOutputStream(file, false);
		out.write(string.getBytes());
		out.close();

	}

	/**
	 * 读文件
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static String readFile(File file) throws IOException {
		StringBuffer sb = new StringBuffer();
		if (!file.exists() || file.isDirectory()) {
			throw new FileNotFoundException();
		}
		BufferedReader buff = new BufferedReader(new FileReader(file));
		String temp = null;
		temp = buff.readLine();
		while (temp != null) {
			sb.append(temp);
			temp = buff.readLine();
		}
		buff.close();
		return sb.toString();
	}

	public static void createBean(String directory, List<TableFile> tableFiles) {

		for (TableFile tableFile : tableFiles) {
			try {
				StringBuilder sb = JavaBuild.create(tableFile);
				saveFile(Roaster.format(sb.toString()), new File(countFileName(directory, tableFile)));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		DialogUtil.showInformation("生成JavaBean", "生成JavaBean成功！");
	}

	public static String countFileName(String directory, TableFile tableFile) {
		String packageName = tableFile.getPackageName();
		packageName = packageName.replace("package", "");
		packageName = packageName.replace(";", "");
		packageName = packageName.replace(".", "/");
		if (directory.endsWith("/")) {
			directory = directory.substring(0, directory.length() - 1);
		}
		return String.format("%s/%s/%s.java", directory, packageName, tableFile.getFileName());

	}

	private static void createFile(File file) {
		File parentFile = new File(file.getParent());
		if (!parentFile.exists()) {
			parentFile.mkdirs();
		}
		if (!file.exists()) {

			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
}
