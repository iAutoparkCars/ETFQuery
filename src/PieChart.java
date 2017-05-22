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

	public void createPieChart() throws IOException
	{
		DefaultPieDataset dataset = new DefaultPieDataset( );
		dataset.setValue("IPhone 5s", new Double( 20 ) );
		dataset.setValue("SamSung Grand", new Double( 20 ) );
		dataset.setValue("MotoG", new Double( 40 ) );
		dataset.setValue("Nokia Lumia", new Double( 10 ) );

		JFreeChart chart = ChartFactory.createPieChart(
			"Mobile Sales",   // chart title
	         dataset,          // data
	         true,             // include legend
	         true,
	         false);
	         
		int width = 540;   /* Width of the image */
		int height = 480;  /* Height of the image */ 
		File pieChart = new File( "PieChart.jpeg" ); 
		ChartUtilities.saveChartAsJPEG( pieChart , chart , width , height );
	}
}
