import java.io.IOException;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;


public class Scrape
{
	public static void main(String args[]) throws IOException
	{
		ReadInput input = new ReadInput();
		Thread inputThread = new Thread(input);
		inputThread.start();
		
	}

	
}

//class to read in user's input and process ETFQuery
	class ReadInput implements Runnable
  {
		Scanner reader;
		public void run()
      {
          
          while (true)
          {
          	System.out.println("Enter the Ticker Symbol of the ETF you would like to query: ");
          	
          	reader = new Scanner(System.in);
          	String chosen_etf = reader.nextLine();
          	
          	try
          	{
          		ETFQuery query = new ETFQuery(chosen_etf);
          		query.getETFPage();
          		query.getETFDetail();
          		query.generateCSV();
          		query.generateHTMLTable();
          	
          	
          	} 
          	catch (IOException e)
          	{e.printStackTrace();
          	System.out.println("Please close the open CSV or HTML files and restart program.");
          	System.exit(0);} 
          	catch
          	(ParserConfigurationException e)
          	{e.printStackTrace();} 
          	catch 
          	(SAXException e) 
          	{e.printStackTrace();} 
          	catch (TransformerException e)
          	{e.printStackTrace();}
          	
          	//reader.close();
          }
      }     
  } //end ReadInput subclass
