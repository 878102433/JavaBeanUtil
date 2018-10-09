package application.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

@XmlRootElement(name = "tableFile")
public class TableFile implements Cloneable {

	private String tableName;
	private String fileName;
	private List<JavaDB> javaDBs;
	private List<String> importNames;
	private List<String> annotationFileNames;
	private String packageName;
	private String comment;

	@Override
	public TableFile clone() {
		TableFile tableFile = null;
		try {
			tableFile = (TableFile) super.clone();
			tableFile.setJavaDBs(null);
			tableFile.setImportNames(null);
			tableFile.setAnnotationFileNames(null);

			if (javaDBs != null) {
				List<JavaDB> javaDBsClone = new ArrayList<JavaDB>();
				for (JavaDB javaDB : javaDBs) {
					javaDBsClone.add(javaDB.clone());
				}
				tableFile.setJavaDBs(javaDBsClone);
			}
			if (importNames != null) {
				List<String> importNamesClone = new ArrayList<String>();
				for (String importName : importNames) {
					importNamesClone.add(importName);
				}
				tableFile.setImportNames(importNamesClone);
			}

			if (annotationFileNames != null) {
				List<String> annotationFileNamesClone = new ArrayList<String>();
				for (String annotationFileName : annotationFileNames) {
					annotationFileNamesClone.add(annotationFileName);
				}
				tableFile.setAnnotationFileNames(annotationFileNamesClone);
			}
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return tableFile;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@XmlElement(name = "javaDB")
	public List<JavaDB> getJavaDBs() {
		return javaDBs;
	}

	public void setJavaDBs(List<JavaDB> javaDBs) {
		this.javaDBs = javaDBs;
	}

	@XmlElement(name = "importName")
	public List<String> getImportNames() {
		return importNames;
	}

	public void setImportNames(List<String> importNames) {
		this.importNames = importNames;
	}

	@XmlElement(name = "annotationFileName")
	public List<String> getAnnotationFileNames() {
		return annotationFileNames;
	}

	public void setAnnotationFileNames(List<String> annotationFileNames) {
		this.annotationFileNames = annotationFileNames;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public ObservableList<JavaDB> getObJavaDB() {
		if (javaDBs == null) {
			return null;
		} else {
			ObservableList<JavaDB> personData = FXCollections.observableArrayList();
			for (JavaDB javaDB : javaDBs) {
				personData.add(javaDB);
			}
			return personData;
		}
	}
}
