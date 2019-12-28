package app;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;


import org.json.JSONArray;
import org.json.JSONObject;

public class TicketMasterAPIManager {
	
	private static HttpURLConnection con;
	
	public static void main(String[] args) {
		
		BufferedReader reader;
		String line;
		StringBuffer responseContent = new StringBuffer();
		
		URL url;
		try {
			//Try today's date
			LocalDateTime start = LocalDateTime.now();
			//End 1 month after
			LocalDateTime end = start.plusMonths(1);

			String urlString = URLManager.searchEventURL(null, start, end, null);
			
			url = new URL(urlString);
			con = (HttpURLConnection) url.openConnection();
			
			//request setup
			con.setRequestMethod("GET");
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);
			con.setRequestProperty("Content-Type", "application/json");
			
			int status = con.getResponseCode();
			//System.out.println(status);
			
			if(status > 299) {
				reader = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			else {
				reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}
			while((line = reader.readLine()) != null) {
				responseContent.append(line);
			}
			reader.close();
			//System.out.println(responseContent.toString());
			parse(responseContent.toString());
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			con.disconnect();
		}
		
	}
	
	//See ExampleEventsResponse.txt for example response body
	public static String parse(String responseBody) {
		JSONObject wholePage = new JSONObject(responseBody);
		JSONObject page = wholePage.getJSONObject("page");
		JSONObject links = wholePage.getJSONObject("_links");
		JSONObject embedded = wholePage.getJSONObject("_embedded");
		
		//Array of events
		JSONArray events = embedded.getJSONArray("events");
		
		int totalElements = page.getInt("totalElements");
		int totalPages = page.getInt("totalPages");
		int size = page.getInt("size");
		System.out.println("Total Elements = " + totalElements);
		System.out.println("Total Pages = " + totalPages);
		System.out.println("Elements per page = " + size);
		
		for(int i = 0; i < events.length() ; i++) {
			//Get each event
			JSONObject event = events.getJSONObject(i);
			String name = event.getString("name");
			//Get event id and url to ticketmaster
			String id = event.getString("id");
			String url = event.getString("url");
			//Get an example image
			JSONArray images = event.getJSONArray("images");
			String firstImage = images.getJSONObject(0).getString("url");
			//Get start DateTime
			JSONObject dates = event.getJSONObject("dates");
			JSONObject start = dates.getJSONObject("start");
			String startDateTime = start.getString("dateTime");
			
			
			System.out.println("Event: ");
			System.out.println("	Name: " + name);
			System.out.println("	Starts At: " + startDateTime);
			System.out.println("	id: " + id);
			System.out.println("	link: " + url);
			System.out.println("	image: " + firstImage);
			//System.out.println(name + "  id=" + id + "  link=" + url + "  image=" + firstImage);
		}
		
		return null;
	}
	
}

