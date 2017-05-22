//data structures to store the EtF's data
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

//chart building libraries
import java.io.*;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;


public class PieChart
{
	String pie_countrypath = "";
	String pie_sectorpath = "";
	
	private String ETF_Symbol;
	private String ETF_Name;
	private String objective;
	private SortedMap<String, Double> countryWeight = new TreeMap<String, Double>();
	private List<SectorItem> sectorList = new ArrayList<SectorItem>();
	
	public PieChart(String symb, String name, SortedMap<String, Double> countryWeight, List<SectorItem> sectorList)
	{
		this.ETF_Symbol = symb;
		this.ETF_Name = name;
		this.countryWeight = countryWeight;
		this.sectorList = sectorList;
	}

	public void createSectorPie() throws IOException
	{
		DefaultPieDataset dataset = new DefaultPieDataset( );

		for (SectorItem i : sectorList)
		{
			dataset.setValue(i.getSector(), new Double( i.getValue() ) );
		}

		JFreeChart chart = ChartFactory.createPieChart(
			ETF_Symbol + " Sector Weights",   // chart title
	         dataset,          // data
	         true,             // include legend
	         true,
	         false);
	         
		int width = 540;   /* Width of the image */
		int height = 480;  /* Height of the image */ 
		String sectorpath = ETF_Symbol + "_SectorPie.jpg";
		this.pie_sectorpath = sectorpath;
		File pieChart = new File( sectorpath ); 
		ChartUtilities.saveChartAsJPEG( pieChart , chart , width , height );
	}
	
	public void createCountryPie() throws IOException
	{
		DefaultPieDataset dataset = new DefaultPieDataset( );

		for (SortedMap.Entry<String,Double> i : countryWeight.entrySet())
		{
			dataset.setValue(i.getKey(), new Double( i.getValue() ) );
		}

		JFreeChart chart = ChartFactory.createPieChart(
			ETF_Symbol + " Country Weights",   // chart title
	         dataset,          // data
	         true,             // include legend
	         true,
	         false);
	         
		int width = 840;   /* Width of the image */
		int height = 720;  /* Height of the image */ 
		String path = ETF_Symbol + "_CountryPie.jpg";
		pie_countrypath = path;
		File pieChart = new File( path ); 
		ChartUtilities.saveChartAsJPEG( pieChart , chart , width , height );
	}
	
	public String getCountryPath()
	{
		return pie_countrypath;
	}

	public String getSectorPath()
	{
		return pie_sectorpath;
	}
}
