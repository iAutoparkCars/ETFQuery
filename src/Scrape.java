import java.io.IOException;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Scrape
{
	public static void main(String args[]) throws IOException
	{
		ReadInput input = new ReadInput();
		Thread inputThread = new Thread(input);
		inputThread.start();
		
		
		/*//get the HTML doc associated with the web page with a timeout
		Document doc1 = Jsoup.connect("http://www.wikihow.com/wikiHowTo?search=signal+wifi").timeout(5000).get();
		
		//Select this whole element specified by the ID #. This element is a list of search results/child elements
		Elements elementList = doc1.select("div#searchresults_list");
		
		
		//For the whole elementList, loop through each element by with class "result", then look into inner class result_thumb
		for (Element e : elementList.select("div.result"))
		{
			Elements result_thumb = e.select("div.result_thumb img");
			String src_url = result_thumb.attr("src");
			System.out.println(src_url);
			
		}*/
		
		/*  //another way to select the elements	
		Elements result_thumb = e.select("div.result_thumb");
		Elements img = result_thumb.select("img");
		System.out.println(img.attr("src"));
		*/
	
		
	}

	
}


//class to read in user's input and process ETFQuery
	class ReadInput implements Runnable
  {
      public void run()
      {
          Scanner reader;
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
          	} 
          	catch (IOException e)
          	{e.printStackTrace();}
          	
          	//reader.close();
          }
      }     
  } //end ReadInput subclass
