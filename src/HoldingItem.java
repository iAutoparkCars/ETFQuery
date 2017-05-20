//Class to logically store the fields of each holding in TOP_TEN_HOLDINGS

public class HoldingItem
{
	private String name;
	private Double weight;
	private Integer shares;
	
	public HoldingItem(String name, Double weight, Integer no_of_shares)
	{
		this.name = name;
		this.weight = weight;
		this.shares = no_of_shares;
	}
	
	
	@Override
	public String toString()
	{
		return name + " " + weight + "% " + shares;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Double getWeight() {
		return weight;
	}
	public void setWeight(Double weight) {
		this.weight = weight;
	}
	public Integer getShares() {
		return shares;
	}
	public void setShares(Integer shares) {
		this.shares = shares;
	}
	
	
}
