package application.model;

public class DbColumn implements Cloneable {
	private String columnName;
	private String dataType;
	private String Arguments;
	private Boolean primaryKey;
	private Boolean nullEnable;
	private String comment;

	@Override
	public DbColumn clone() {
		DbColumn dbColumn = null;
		try {
			dbColumn = (DbColumn) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return dbColumn;
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

	public String getArguments() {
		return Arguments;
	}

	public void setArguments(String arguments) {
		Arguments = arguments;
	}

	public Boolean getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(Boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	public Boolean getNullEnable() {
		return nullEnable;
	}

	public Boolean isNullEnable() {
		return nullEnable;
	}

	public void setNullEnable(Boolean nullEnable) {
		this.nullEnable = nullEnable;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
