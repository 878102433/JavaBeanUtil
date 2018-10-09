package application.model;

public class TableBean implements Cloneable {
	public static final String TYPE_TABLE_ROOT = "TYPE_TABLE_ROOT";
	public static final String TYPE_TABLE = "TYPE_TABLE";
	public static final String TYPE_PACKAGE_ROOT = "TYPE_PACKAGE_ROOT";
	public static final String TYPE_PACKAGE = "TYPE_PACKAGE";
	public static final String TYPE_BEAN = "TYPE_BEAN";
	private String type;
	private String name;
	private String linkName;
	private String parentName;
	private boolean isUsed;
	private String comment;

	public TableBean() {

	}

	public TableBean(String type, String name) {
		this.type = type;
		this.name = name;
	}

	public TableBean(String type, String name, String linkName) {
		this.type = type;
		this.name = name;
		this.linkName = linkName;
	}

	public TableBean(String type, String name, String linkName, String parentName) {
		this.type = type;
		this.name = name;
		this.linkName = linkName;
		this.parentName = parentName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLinkName() {
		return linkName;
	}

	public void setLinkName(String linkName) {
		this.linkName = linkName;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public String toString() {
		return name;

	}

	public boolean isUsed() {
		return isUsed;
	}

	public void setUsed(boolean isUsed) {
		this.isUsed = isUsed;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
