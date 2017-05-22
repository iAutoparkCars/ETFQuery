import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.ChartUtilities;


public class BarChart
{	
	//ETF name, objective, holdings, country weights, and sector weights
	private String ETF_Symbol;
	private String ETF_Name;
	private String objective;
	private ArrayList<HoldingItem> holdings;
	private SortedMap<String, Double> countryWeight = new TreeMap<String, Double>();
	private List<SectorItem> sectorList = new ArrayList<SectorItem>();
	private String chartpath;
	
	public BarChart(String symb, String name, ArrayList<HoldingItem> holdings)
	{
		this.ETF_Symbol = symb;
		this.ETF_Name = name;
		this.holdings = holdings;
	}
	   
	public String getChartPath()
	{
		return chartpath;
	}
	
	public void createBarChart() throws IOException
	{
		final String fiat = "FIAT";
		final String audi = "AUDI";
		final String ford = "FORD";
		
		final String speed = "Speed";
		final String millage = "Millage";
		final String userrating = "User Rating";
		final String safety = "safety";

		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		
		for (HoldingItem i : holdings)
		{
			dataset.addValue( i.getWeight() , i.getName() , i.getShares());
		}

		JFreeChart barChart = ChartFactory.createBarChart(
				"HOLDINGS for " + ETF_Name, 
	         "Country & No. of Shares", "Weight", 
	         dataset,PlotOrientation.VERTICAL, 
	         true, true, false);
	         
	      int width = 540;    /* Width of the image */
	      int height = 480;   /* Height of the image */ 
	      String chartname = ETF_Symbol.toUpperCase() + "_" + "BarChart.jpg";
	      this.chartpath = chartname;
	      File BarChart = new File(chartname); 
	      ChartUtilities.saveChartAsJPEG(BarChart , barChart , width , height );
	   }
	
	  
}