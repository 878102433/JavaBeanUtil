package application.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ForeignColumn {

	private final StringProperty tableName;
	private final StringProperty columnName;
	private final StringProperty foreignTableName;
	private final StringProperty foreignColumnName;
	private final BooleanProperty isChecked;

	public ForeignColumn() {
		this(null, null, null, null, null);
	}

	public ForeignColumn(String tableName, String columnName, String foreignTableName, String foreignColumnName, Boolean isChecked) {
		this.tableName = new SimpleStringProperty(tableName);
		this.columnName = new SimpleStringProperty(columnName);
		this.foreignTableName = new SimpleStringProperty(foreignTableName);
		this.foreignColumnName = new SimpleStringProperty(foreignColumnName);
		this.isChecked = new SimpleBooleanProperty(isChecked);
	}

	public String getTableName() {
		return tableName.get();
	}

	public void setTableName(String tableName) {
		this.tableName.set(tableName);
	}

	public StringProperty tableNameProperty() {
		return tableName;
	}

	public String getColumnName() {
		return columnName.get();
	}

	public void setColumnName(String columnName) {
		this.columnName.set(columnName);
	}

	public StringProperty columnNameProperty() {
		return columnName;
	}

	public String getForeignTableName() {
		return foreignTableName.get();
	}

	public void setForeignTableName(String foreignTableName) {
		this.foreignTableName.set(foreignTableName);
	}

	public StringProperty foreignTableNameProperty() {
		return foreignTableName;
	}

	public String getForeignColumnName() {
		return foreignColumnName.get();
	}

	public void setForeignColumnName(String foreignColumnName) {
		this.foreignColumnName.set(foreignColumnName);
	}

	public StringProperty foreignColumnNameProperty() {
		return foreignColumnName;
	}

	public Boolean isChecked() {
		return isChecked.get();
	}

	public void setChecked(Boolean isChecked) {
		this.isChecked.set(isChecked);
	}

	public BooleanProperty isCheckedProperty() {
		return isChecked;
	}
}