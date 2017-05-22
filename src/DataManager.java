import java.sql.*;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class DataManager
{
	public String ETF_Symbol;
	private String ETF_Name;
	private String objective;
	public ArrayList<HoldingItem> holdings;
	public SortedMap<String, Double> countryWeight = new TreeMap<String, Double>();
	public List<SectorItem> sectorList = new ArrayList<SectorItem>();
	
	static String database = "jdbc:mysql://localhost:3306/demo";
	static String user = "orbis";
	static String password = "orbis";
	
	public DataManager(){}
	
	public DataManager(String symb){}
	
	
	
	public ArrayList<HoldingItem> fetchHoldings()
	{
		ArrayList<HoldingItem> holdingList = new ArrayList<HoldingItem>();;
		try
		{
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/demo",user,password);
			
			Statement myStat = conn.createStatement();
			
			ResultSet myRes = myStat.executeQuery("select * from holdings");
			
			while(myRes.next())
			{
				String name = myRes.getString("name");
				Double weight = myRes.getDouble("weight");
				Integer shares = myRes.getInt("shares");
				
				//System.out.println(myRes.getString("name") + " " + myRes.getDouble("weight") + " " + myRes.getInt("shares"));
				HoldingItem item = new HoldingItem(name, weight, shares);
				holdingList.add(item);
			}
		}
		catch(Exception e)
		{e.printStackTrace();}
		
		for (HoldingItem i : holdingList)
		{
			System.out.println(i.toString());
		}
		return holdingList;
	}
	
	public void insertHoldings(ArrayList<HoldingItem> list)
	{
		try
		{
			Connection conn = DriverManager.getConnection(database,user,password);
			
			Statement myStat = conn.createStatement();
			
			for (HoldingItem i : list)
			{
				String sql = "insert into holdings " 
						+ " (name, weight, shares) "
						+ "values('"+i.getName()+"', '"+i.getWeight()+"','"+i.getShares()+"')";
				myStat.executeUpdate(sql);
			}
			
		}
		catch(Exception e)
		{e.printStackTrace();}
	}

	public void insertSectors(List<SectorItem> list)
	{
		try
		{
			Connection conn = DriverManager.getConnection(database,user,password);
			
			Statement myStat = conn.createStatement();
			
			for (SectorItem i : list)
			{
				String sql = "insert into sectors " 
						+ " (sector, weight) "
						+ "values('"+i.getSector()+"', '"+i.getValue()+"')";
				myStat.executeUpdate(sql);
			}
			
			
		}
		catch(Exception e)
		{e.printStackTrace();}
	}
	
	public void insertCountries(SortedMap<String, Double> countries)
	{
		try
		{
			Connection conn = DriverManager.getConnection(database,user,password);
			
			Statement myStat = conn.createStatement();
			
			for(SortedMap.Entry<String,Double> e : countries.entrySet())
			{
				String sql = "insert into countries " 
						+ " (country, weight) "
						+ "values('"+e.getKey()+"', '"+e.getValue()+"')";
				myStat.executeUpdate(sql);
			}
			
		}
		catch(Exception e)
		{e.printStackTrace();}
	}
}
