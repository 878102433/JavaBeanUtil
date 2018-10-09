package application.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class JavaColumn implements Cloneable {
	private String columnName;
	private String dataType;
	private List<String> importNames;
	private List<String> annotationGetNames;
	private List<String> annotationSetNames;

	@Override
	public JavaColumn clone() {
		JavaColumn javaColumn = null;
		try {
			javaColumn = (JavaColumn) super.clone();
			javaColumn.setImportNames(null);
			javaColumn.setAnnotationGetNames(null);
			javaColumn.setAnnotationSetNames(null);
			if (importNames != null) {
				List<String> importNamesClone = new ArrayList<String>();
				for (String importName : importNames) {
					importNamesClone.add(importName);
				}
				javaColumn.setImportNames(importNamesClone);
			}
			if (annotationGetNames != null) {
				List<String> annotationGetNamesClone = new ArrayList<String>();
				for (String annotationGetName : annotationGetNames) {
					annotationGetNamesClone.add(annotationGetName);
				}
				javaColumn.setAnnotationGetNames(annotationGetNamesClone);
			}
			if (annotationSetNames != null) {
				List<String> annotationSetNamesClone = new ArrayList<String>();
				for (String annotationSetName : annotationSetNames) {
					annotationSetNamesClone.add(annotationSetName);
				}
				javaColumn.setAnnotationSetNames(annotationSetNamesClone);
			}
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return javaColumn;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	@XmlElement(name = "importName")
	public List<String> getImportNames() {
		return importNames;
	}

	public void setImportNames(List<String> importNames) {
		this.importNames = importNames;
	}

	@XmlElement(name = "annotationGetName")
	public List<String> getAnnotationGetNames() {
		return annotationGetNames;
	}

	public void setAnnotationGetNames(List<String> annotationGetNames) {
		this.annotationGetNames = annotationGetNames;
	}

	@XmlElement(name = "annotationSetName")
	public List<String> getAnnotationSetNames() {
		return annotationSetNames;
	}

	public void setAnnotationSetNames(List<String> annotationSetNames) {
		this.annotationSetNames = annotationSetNames;
	}

}
