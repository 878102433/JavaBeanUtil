package application.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class JavaDB implements Cloneable {

	private DbColumn dbColumn;
	private JavaColumn javaColumn;

	@Override
	public JavaDB clone() {
		JavaDB javaDB = null;
		try {
			javaDB = (JavaDB) super.clone();
			javaDB.setDbColumn(null);
			javaDB.setJavaColumn(null);
			javaDB.dbColumn = dbColumn.clone();
			javaDB.javaColumn = javaColumn.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return javaDB;
	}

	public DbColumn getDbColumn() {
		return dbColumn;
	}

	public void setDbColumn(DbColumn dbColumn) {
		this.dbColumn = dbColumn;
	}

	public JavaColumn getJavaColumn() {
		return javaColumn;
	}

	public void setJavaColumn(JavaColumn javaColumn) {
		this.javaColumn = javaColumn;
	}

	public StringProperty dataTypeProperty() {
		return new SimpleStringProperty(dbColumn.getDataType());
	}

	public StringProperty argumentsProperty() {
		return new SimpleStringProperty(dbColumn.getArguments());
	}

	public StringProperty primaryKeyProperty() {
		if (dbColumn.getPrimaryKey() == null) {
			return new SimpleStringProperty("否");
		} else if (dbColumn.getPrimaryKey()) {
			return new SimpleStringProperty("是");
		} else {
			return new SimpleStringProperty("否");
		}

	}

	public StringProperty nullEnableProperty() {
		if (dbColumn.getNullEnable() == null) {
			return new SimpleStringProperty("否");
		} else if (dbColumn.getNullEnable()) {
			return new SimpleStringProperty("是");
		} else {
			return new SimpleStringProperty("否");
		}

	}

	public StringProperty javaTypeProperty() {
		return new SimpleStringProperty(javaColumn.getDataType());
	}
}
