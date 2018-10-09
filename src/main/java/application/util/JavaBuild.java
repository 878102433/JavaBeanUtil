package application.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import application.model.DbColumn;
import application.model.JavaColumn;
import application.model.JavaDB;
import application.model.TableFile;

public class JavaBuild {
	public static StringBuilder buffer;

	public JavaBuild() {

	}

	public static StringBuilder create(TableFile tableFile) {
		List<JavaDB> javaDBs = tableFile.getJavaDBs();
		List<JavaColumn> javaColumns = new ArrayList<JavaColumn>();
		for (JavaDB javaDB : javaDBs) {
			javaColumns.add(javaDB.getJavaColumn());
		}
		buffer = new StringBuilder();

		// 打包名
		if (tableFile.getPackageName() != null) {
			appendln(String.format("%s%s", tableFile.getPackageName(), tableFile.getPackageName().endsWith(";") ? "" : ";"));
		}

		// 导入包
		List<String> importNames = tableFile.getImportNames();
		List<String> importNamesAll = new ArrayList<String>();
		if (importNames != null) {
			for (String importName : importNames) {
				importNamesAll.add(String.format("%s%s", importName, importName.endsWith(";") ? "" : ";"));
			}
		}
		for (JavaColumn javaColumn : javaColumns) {
			importNames = javaColumn.getImportNames();
			if (importNames != null) {
				for (String importName : importNames) {
					importNamesAll.add(String.format("%s%s", importName, importName.endsWith(";") ? "" : ";"));
				}
			}

		}
		// 去重
		List<String> listWithoutDup = new ArrayList<>(new HashSet<>(importNamesAll));
		for (String improtName : listWithoutDup) {
			if (!StringUtil.isEmptyOrNull(improtName)) {
				appendln(improtName);
			}

		}

		// 文件名
		List<String> annotationFileNames = tableFile.getAnnotationFileNames();
		if (annotationFileNames != null) {
			for (String annotationFileName : annotationFileNames) {
				appendln(annotationFileName, "#tableName", String.format("\"%s\"", tableFile.getTableName()));
			}
		}
		appendln(String.format("public class %s implements Serializable{", tableFile.getFileName()));
		if (tableFile.getComment() != null) {
			appendln(String.format("/**\r\n *%s\r\n*/", tableFile.getComment().replaceAll("\r\n", "").replaceAll("\n", "")));
		}

		// appendln("// TODO 格式化并生成serialVersionUID");

		// serialVersionUID
		appendln(String.format("private static final long serialVersionUID = %sL;", new Random().nextLong()));
		// 属性
		List<String> cloumns = new ArrayList<String>();
		List<String> equalColumns = new ArrayList<String>();
		for (JavaDB javaDB : javaDBs) {
			JavaColumn javaColumn = javaDB.getJavaColumn();
			DbColumn dbColumn = javaDB.getDbColumn();
			appendln(String.format("/** %s */", StringUtil.null2Empty(dbColumn.getComment()).replaceAll("\r\n", "").replaceAll("\n", "")));
			appendln(String.format("private %s %s;", javaColumn.getDataType(), javaColumn.getColumnName()));
			cloumns.add(String.format("%s %s", javaColumn.getDataType(), javaColumn.getColumnName()));
			equalColumns.add(String.format("this.%s=%s;", javaColumn.getColumnName(), javaColumn.getColumnName()));
		}

		// 无参构造方法
		appendln(String.format("public %s (){}", tableFile.getFileName()));

		// 有参构造方法
		// appendln(String.format("public %s(%s){%s}", tableFile.getFileName(),
		// StringUtils.join(cloumns.toArray(), ","),
		// StringUtils.join(equalColumns.toArray(), "\r\n")));

		// get和set
		for (JavaDB javaDB : javaDBs) {
			JavaColumn javaColumn = javaDB.getJavaColumn();
			DbColumn dbColumn = javaDB.getDbColumn();
			List<String> annotationGetNames = javaColumn.getAnnotationGetNames();
			if (annotationGetNames != null) {
				for (String annotationGetName : annotationGetNames) {
					appendln(annotationGetName, "#cloumnName", String.format("\"%s\"", dbColumn.getColumnName()));
				}
			}
			appendln(createGet(javaColumn));

			List<String> annotationSetNames = javaColumn.getAnnotationSetNames();
			if (annotationSetNames != null) {
				for (String annotationSetName : annotationSetNames) {
					appendln(annotationSetName, "#cloumnName", String.format("\"%s\"", dbColumn.getColumnName()));
				}
			}
			appendln(String.format("public void set%s(%s %s){this.%s=%s;}", StringUtil.upperCaseFirstLetter(javaColumn.getColumnName()), javaColumn.getDataType(), javaColumn.getColumnName(), javaColumn.getColumnName(), javaColumn.getColumnName()));
		}
		appendln("}");

		return buffer;

	}

	/**
	 * 生成get方法
	 *
	 * @param javaColumn
	 * @return
	 */
	private static String createGet(JavaColumn javaColumn) {
		if ("boolean".equals(javaColumn.getDataType()) || "Boolean".equals(javaColumn.getDataType())) {
			String methodName = "";
			if (javaColumn.getColumnName().startsWith("is")) {
				methodName = javaColumn.getColumnName();
			} else {
				methodName = String.format("is%s", StringUtil.upperCaseFirstLetter(javaColumn.getColumnName()));
			}
			return String.format("public %s %s(){return this.%s;}", javaColumn.getDataType(), methodName, javaColumn.getColumnName());
		} else {
			return String.format("public %s get%s(){return this.%s;}", javaColumn.getDataType(), StringUtil.upperCaseFirstLetter(javaColumn.getColumnName()), javaColumn.getColumnName());
		}
	}

	public static void append(Object object) {
		buffer.append(object);
	}

	public static void appendln() {
		buffer.append("\r\n");
	}

	public static void appendln(Object object) {
		buffer.append(object + "\r\n");
	}

	public static void append(String string, String regex, String replacement) {
		buffer.append(string.replaceAll(regex, replacement));
	}

	public static void appendln(String string, String regex, String replacement) {
		buffer.append(string.replaceAll(regex, replacement) + "\r\n");
	}
}
