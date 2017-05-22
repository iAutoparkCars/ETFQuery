
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;


public class ETFQuery
{
	private Document search_result;
	private String baseURL = "https://us.spdrs.com/en";
	public String ETF_Symbol;
	public String html_path = "";
	
	//information to parse from detailed ETF URL
	private String objective;
	private String ETF_Name;
	public ArrayList<HoldingItem> holdings;
	
	//Name is key, weight is the value
	//public SortedMap<String, Double> sectorWeight = new TreeMap<String, Double>();
	public SortedMap<String, Double> countryWeight = new TreeMap<String, Double>();
	public List<SectorItem> sectorList = new ArrayList<SectorItem>();
	public String sectorWeight = "";
	
	public ETFQuery (String ETF_Symbol) throws IOException
	{
			//get the HTML doc associated with the web page with a timeout
		search_result = Jsoup.connect(baseURL).timeout(5000).get();
		this.ETF_Symbol = ETF_Symbol.toUpperCase();
		holdings = new ArrayList<HoldingItem>();
		
	}
	
	//gets the URL that displays detailed information about the specified ETF_Symbol
	public void getETFPage()
	{
		Elements menu_list = search_result.select("nav#SPDR_nav").select("div#menu_ETFs");
		
			//counter to specify the International and US ETF_Symbol's may be querried
		int counter = 0;
		for (Element e : menu_list.select("div.subsubmenu_lists"))
		{
			
			
			if (counter == 1 || counter == 2)
			{
				for (Element href : e.select("a") )
				{
					String href_link = href.attr("href");
					if (href_link.contains(ETF_Symbol.toUpperCase()))
					{	
						baseURL = baseURL + href_link;
						//System.out.println(baseURL);
					}
				}
			}
				counter++;
		}
	}
	
	public void getETFDetail() throws IOException
	{
		getETFName();
		getETFObjective();
		getETFHoldings();
		getETFCountry();
		getETFSector();
		
		//insert all data in DB. This section is commented because I currently 
		//do not want to re-insert Primary Keys (induces error)
		DataManager a = new DataManager();
		//a.insertHoldings(holdings);
		//a.insertSectors(sectorList);
		//a.insertCountries(countryWeight);
	}
	
	//this function gets the document, so this MUST be called before the other getETF* functions
	private void getETFName() throws IOException
	{
			//loading the document from detailed ETF page
		search_result = Jsoup.connect(baseURL).timeout(5000).get();
		
		ETF_Name = search_result.select("title").first().text();
		//System.out.println(ETF_Name);
	}

	public void getETFObjective() throws IOException
	{
			//get objective info
		Elements objectiveAttr = search_result.select("div.overview").select("div.col2s p");
		objective = objectiveAttr.first().text();
		objective = objective + " " + objectiveAttr.next().first().text();
		
		//System.out.println(objective);
	}
	
	private void getETFHoldings()
	{
		Elements holdingsAttr = search_result.select("div#FUND_TOP_TEN_HOLDINGS");
		
		for (int i = 0; i < holdingsAttr.select("td").size()/3; i++)
		{
			String name = holdingsAttr.select("td").get(3*i+0).text();
			String w[] = holdingsAttr.select("td").get(3*i+1).text().trim().split(" ");
			Double weight = Double.valueOf(w[0]);
			Integer shares = Integer.valueOf(holdingsAttr.select("td").get(3*i+2).text().replace(",", ""));
			
			//System.out.println(name + " " + weight + " " + shares);
			
			HoldingItem item = new HoldingItem(name, weight, shares);
			holdings.add(item);
		}
	
		/*for (HoldingItem i : holdings)
		{
			System.out.println(i.toString());
		}
		System.out.println("");*/
	}

	private void getETFCountry()
	{
		Elements country_weights = search_result.select("div.col2s.leftm");
		
		for (int i = 0; i < country_weights.size(); i++)
		{
			if (country_weights.get(i).text().contains("Country Weights"))
			{
				/*for (Element f : country_weights.get(i).select("td"))
				{
					System.out.println(f.text());
				}*/
				
				for (int j = 0; j < country_weights.get(i).select("td").size()/2; j++)
				{
					String country = country_weights.get(i).select("td").get(2*j+0).text();
					Double weight = Double.valueOf(country_weights.get(i).select("td").get(2*j+1).text().replace("%", ""));
					
					//System.out.println(country + " " + weight + "%");
					countryWeight.put(country, weight);
					
					//System.out.println(country_weights.get(i).select("td").get(2*j+0).text());
					//System.out.println(country_weights.get(i).select("td").get(2*j+1).text());

				}
				//System.out.println(country_weights.get(i).select("tr").select("td.label").text());
				//System.out.println(country_weights.get(i).select("td.data").text());
			}
		}	//end outer for loop
		
		//System.out.println(countryWeight.keySet() + " " + countryWeight.size());
		
	}

	private void getETFSector()
	{
		Elements divWithId = search_result.select("div#FUND_SECTOR").select("div#SectorsAllocChart");
		
		//get the XML brackets
		sectorWeight = divWithId.text().replace("&lt;", "<").replace("&gt;",">");

		//collect attributes and store into list of sectors
		Document doc = Jsoup.parse(sectorWeight, "", Parser.xmlParser());
		for (Element e : doc.select("attribute"))
		{
			String value = e.select("rawValue").text();
			String sector = e.select("label").text();
			
			SectorItem item = new SectorItem(sector,value);
			sectorList.add(item);
		}
		
		//System.out.println(sectorWeight);
		//some function to conver this XML file into CSV
	}

	public void generateCSV() throws ParserConfigurationException, SAXException, IOException, TransformerException
	{
			//initialize the tables
		String countryStr = "";
		String holdingStr = "";
		String sectorStr = "";
		
			//specify the output CSV
		String filename = ETF_Symbol.toUpperCase() + "_information.csv";
		PrintWriter pw = new PrintWriter(new File(filename));
	
			//create the tables
		countryStr = createHoldingsTable();

		if (!countryWeight.isEmpty())
			holdingStr = createCountryTable();
		
		sectorStr = createSectorTable();
		
			//createCSVSector();
		pw.write(countryStr + holdingStr + sectorStr);
        pw.close();
        
        //open the CSV
        //openFile(filename);
	}

	public void openFile(String filename)
	{
		if (Desktop.isDesktopSupported())
        {
            try 
            {
                File csvFile = new File(filename);
                Desktop.getDesktop().open(csvFile);
            } 
            catch (IOException ex){}
        }
	}
	
	private String createHoldingsTable()
	{
		StringBuilder str = new StringBuilder();
		
		//create ID for this table
		str.append("TABLE_ID = TOP_TEN_HOLDINGS");
		str.append('\n');
		
		//label each column
		str.append("Name");
        str.append(',');
        str.append("Weight");
        str.append(',');
        str.append("Shares Held");
        str.append('\n');
        
        //values for each column with a for loop
        for (HoldingItem i : holdings)
        {
        	str.append(i.getName());
            str.append(',');
            str.append(i.getWeight()+"%");
            str.append(',');
            str.append(i.getShares());
            str.append('\n');
        }
        str.append('\n');
        
        return str.toString();
	}

	private String createCountryTable()
	{
		StringBuilder str = new StringBuilder();
		
			//create ID for this table
		str.append("TABLE_ID = COUNTRY_WEIGHTS");
		str.append('\n');
		
			//label each column
		str.append("Country");
        str.append(',');
        str.append("Weight");
        str.append('\n');
        
        	//get values from hashmap by iterating through each entry (String,Double pair)
        for(SortedMap.Entry<String,Double> entry : countryWeight.entrySet())
        {
      		str.append(entry.getKey());
            str.append(',');
            str.append(entry.getValue()+"%");
            str.append('\n');
      		//System.out.println(country + " " + weight);
      	}       
        
        str.append('\n');

        return str.toString();
	}

	private String createSectorTable() 
	{
		StringBuilder str = new StringBuilder();

		str.append("TABLE_ID = SECTOR_WEIGHTS");
		str.append('\n');
		
		//label each column
		str.append("Sector");
		str.append(',');
		str.append("Weight");
		str.append('\n');
		
		for(SectorItem i : sectorList)
		{
			str.append(i.getSector());
			str.append(',');
			str.append(i.getValue()+"%");
			str.append('\n');
		}       
	        
	    str.append('\n');
		
		
		return str.toString();
	}
	
	public void generateHTMLTable() throws IOException
	{
		StringBuilder open = new StringBuilder();
		StringBuilder close = new StringBuilder();
		String title = "";
		String holdingTable = "";
		String countryTable = "";
		String sectorTable = "";
		
		String filename = ETF_Symbol.toUpperCase() + "_Table.html";
		html_path = filename;
		File file = new File(filename);
		PrintWriter pw = new PrintWriter(file);
		
		//add opening tags
		open.append("<!DOCTYPE html>");
		open.append("<html>" + '\n');
		
			//opening tags for style
			open.append("<head>" + '\n');
			open.append("<style>" + '\n');
		
			//table structure
			open.append("table{font-family: arial, sans-serif; border-collapse: collapse; width: 100%;}");
			open.append("td, th {border: 1px solid #dddddd;   text-align: left;  padding: 8px; }");
			open.append("tr:nth-child(even) {background-color: #dddddd;}");
		
			//closing tags for style
			open.append("</style>" + '\n');
			open.append("</head>" + '\n');
		
				open.append("<body>" + '\n');
				
		
		title = createTitleHTML();		
		holdingTable = createHoldingsHTML();
		if (!countryWeight.isEmpty())
			countryTable = createCountryHTML();
		sectorTable = createSectorHTML();
		
		//add closing tags
				close.append("</body>" + '\n');
		close.append("</html>" + '\n');
		
		//creating the HTML for the charts
		
		
		//write to file
		String result = open.toString() + title + holdingTable + countryTable + sectorTable + close.toString();
		pw.write(result);
		pw.close();
		
	}
	
	private String createTitleHTML()
	{
		StringBuilder str = new StringBuilder();
		
		str.append("<table>" + '\n');
		str.append("<h1>"+ETF_Name+"</h1>" + '\n');
		str.append("<table STYLE=\"margin-bottom: 30px;\"><tr><td><h4>"+objective+
				"</h4></td></tr> </table> " + '\n');
		str.append("</table>" + '\n');
		
		return str.toString();
	}
	
	private String createHoldingsHTML() throws IOException
	{
		StringBuilder countryStr = new StringBuilder();
		
		countryStr.append("<table>" + '\n');
		countryStr.append("TOP_TEN_HOLDINGS" + '\n');
			
			//labeling the columns
			countryStr.append("<tr>" + '\n');
				countryStr.append("<th>Name</th>" + '\n');
				countryStr.append("<th>Weight</th>" + '\n');
				countryStr.append("<th>Shares</th>" + '\n');
			countryStr.append("<tr>" + '\n');
			
			//printing the entries
			for (HoldingItem i : holdings)
			{
				countryStr.append("<tr>" + '\n');
					countryStr.append("<td>"+i.getName()+"</td>" + '\n');
					countryStr.append("<td>"+i.getWeight()+"%"+"</td>" + '\n');
					countryStr.append("<td>"+i.getShares()+"</td>" + '\n');
				countryStr.append("<tr>" + '\n');
			}

		countryStr.append("</table>" + '\n');
		countryStr.append("<table STYLE=\"margin-bottom: 30px;\"><tr><td></td></tr></table>" + '\n');
		
		//crete and append the bar chart
		BarChart bar = new BarChart(ETF_Symbol, ETF_Name.toUpperCase(), holdings);
		bar.createBarChart();
				
		//PieChart pie = new PieChart(ETF_Symbol, ETF_Name.toUpperCase(), countryWeight, sectorList);
		//pie.createSectorPie();
		
		/*if(!countryWeight.isEmpty())
		{pie.createCountryPie();}*/
		
		
		countryStr.append("<img src=\"" + bar.getChartPath() + "\" alt=\"\" style=\"width:540px;height:480px;\" >");
		countryStr.append("<table STYLE=\"margin-bottom: 30px;\"><tr><td></td></tr></table>" + '\n');
		
		return countryStr.toString();
	}
	
	private String createCountryHTML() throws IOException
	{
		StringBuilder countryStr = new StringBuilder();
		
		countryStr.append("<table>" + '\n');
		countryStr.append("COUNTRY_WEIGHTS" + '\n');
			
			//labeling the columns
			countryStr.append("<tr>" + '\n');
				countryStr.append("<th>Country</th>" + '\n');
				countryStr.append("<th>Weight</th>" + '\n');
			countryStr.append("<tr>" + '\n');
			
			//printing the entries
			for (SortedMap.Entry<String,Double> entry : countryWeight.entrySet())
			{
				countryStr.append("<tr>" + '\n');
					countryStr.append("<td>"+entry.getKey() +"</td>" + '\n');
					countryStr.append("<td>"+entry.getValue() +"%"+"</td>" + '\n');
				countryStr.append("<tr>" + '\n');
			}

		countryStr.append("</table>" + '\n');
		countryStr.append("<table STYLE=\"margin-bottom: 30px;\"><tr><td></td></tr></table>" + '\n');
		
		//creating the pie and adding the resulting png chart to the HTML
		PieChart pie = new PieChart(ETF_Symbol, ETF_Name.toUpperCase(), countryWeight, sectorList);
		pie.createCountryPie();
		
		countryStr.append("<img src=\"" + pie.getCountryPath() + "\" alt=\"\" style=\"width:640px;height:540px;\" >");
		countryStr.append("<table STYLE=\"margin-bottom: 30px;\"><tr><td></td></tr></table>" + '\n');
		
		return countryStr.toString();

	}

	private String createSectorHTML() throws IOException
	{
		StringBuilder sectorStr = new StringBuilder();
		
		sectorStr.append("<table>" + '\n');
		sectorStr.append("SECTOR_WEIGHTS" + '\n');
			
			//labeling the columns
			sectorStr.append("<tr>" + '\n');
				sectorStr.append("<th>Sector</th>" + '\n');
				sectorStr.append("<th>Value</th>" + '\n');
			sectorStr.append("<tr>" + '\n');
			
			//printing the entries
			for (SectorItem i : sectorList)
			{
				sectorStr.append("<tr>" + '\n');
					sectorStr.append("<td>"+i.getSector() +"</td>" + '\n');
					sectorStr.append("<td>"+i.getValue() +"%"+"</td>" + '\n');
				sectorStr.append("<tr>" + '\n');
			}

		sectorStr.append("</table>" + '\n');
		sectorStr.append("<table STYLE=\"margin-bottom: 30px;\"><tr><td></td></tr></table>" + '\n');
		
		PieChart pie = new PieChart(ETF_Symbol, ETF_Name.toUpperCase(), countryWeight, sectorList);
		pie.createSectorPie();
		
		sectorStr.append("<img src=\"" + pie.getSectorPath() + "\" alt=\"\" style=\"width:540px;height:480px;\" >");
		sectorStr.append("<table STYLE=\"margin-bottom: 30px;\"><tr><td></td></tr></table>" + '\n');
		
		return sectorStr.toString();

	}

	public String getHTMLPath()
	{
		return html_path;
	}
	
}
