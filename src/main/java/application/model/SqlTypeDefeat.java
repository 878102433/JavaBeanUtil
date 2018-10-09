package application.model;

public class SqlTypeDefeat {
	private String dataType;
	private Integer dataLength;
	private Integer dataPrecision;
	private Integer dataScale;

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public Integer getDataLength() {
		return dataLength;
	}

	public void setDataLength(Integer dataLength) {
		this.dataLength = dataLength;
	}

	public Integer getDataPrecision() {
		return dataPrecision;
	}

	public void setDataPrecision(Integer dataPrecision) {
		this.dataPrecision = dataPrecision;
	}

	public Integer getDataScale() {
		return dataScale;
	}

	public void setDataScale(Integer dataScale) {
		this.dataScale = dataScale;
	}

}
