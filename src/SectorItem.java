
public class SectorItem
{
	private String sector = "";
	private String value = "";
	
	public SectorItem(String sector, String value)
	{
		this.sector = sector;
		this.value = value;
	}
	
	
	public String getSector() {
		return sector;
	}


	public void setSector(String sector) {
		this.sector = sector;
	}


	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	}


	public Double getValueDouble()
	{
		return Double.valueOf(value);
	}

}
