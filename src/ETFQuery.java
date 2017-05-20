
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import java.io.PrintWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;


public class ETFQuery
{
	private Document search_result;
	private String baseURL = "https://us.spdrs.com/en";
	public String ETF_Symbol;
	
	//information to parse from detailed ETF URL
	private String objective;
	private String ETF_Name;
	public ArrayList<HoldingItem> holdings;
	
	//Name is key, weight is the value
	//public SortedMap<String, Double> sectorWeight = new TreeMap<String, Double>();
	public SortedMap<String, Double> countryWeight = new TreeMap<String, Double>();
	public List<SectorItem> sectorList = new ArrayList<SectorItem>();
	public String sectorWeight = "";
	
	
	//public HashMap<String, Double> sectorWeight = new HashMap<String,Double>();
	//public HashMap<String, Double> countryWeight = new HashMap<String,Double>();

	
	
	public ETFQuery (String ETF_Symbol) throws IOException
	{
			//get the HTML doc associated with the web page with a timeout
		search_result = Jsoup.connect(baseURL).timeout(5000).get();
		this.ETF_Symbol = ETF_Symbol;
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
		String holdingStr = "";
		String countryStr = "";
		String sectorStr = "";
		
			//specify the output CSV
		String filename = ETF_Symbol.toUpperCase() + "_information.csv";
		PrintWriter pw = new PrintWriter(new File(filename));
	
			//create the tables
		holdingStr = createHoldingsTable(pw);

		if (!countryWeight.isEmpty())
			countryStr = createCountryTable(pw);
		
		sectorStr = createSectorTable(pw);
		
			//createCSVSector();
		pw.write(holdingStr + countryStr + sectorStr);
        pw.close();
        
        //open the CSV
        //openCSVFile(filename);
	}

	private void openCSVFile(String filename)
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
	
	private String createHoldingsTable(PrintWriter pWriter)
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

	private String createCountryTable(PrintWriter pWriter)
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

	private String createSectorTable(PrintWriter pWriter) 
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
	
}
