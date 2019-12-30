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
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TicketMasterAPIManager {
	
	private static HttpURLConnection con;
	
//	public static void main(String[] args) {
//		
//		//findEvents();
//		//getClassifications();
//	}
	
	/*
	 * Will return something later, perhaps an ArrayList of Event objects
	 */
	public static void findEvents(String postalCode, LocalDateTime start, LocalDateTime end, String[] genres) {
		BufferedReader reader;
		String line;
		StringBuffer responseContent = new StringBuffer();
		URL url;
		try {
			String urlString = URLManager.searchEventURL(postalCode, start, end, genres);
			
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
	
	public static ArrayList<String> getClassifications() {
		BufferedReader reader;
		String line;
		StringBuffer responseContent = new StringBuffer();
		
		URL url;
		try {

			String urlString = URLManager.classificationsURL();
			
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
			parseClassifications(responseContent.toString());
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			con.disconnect();
		}
		
		return null;
	}
	
	
	/*
	 * Returns a Hash Map with the primary classification as the key an array of subtypes/genres as the value
	 */
	public static HashMap<String, ArrayList<String>> parseClassifications(String responseBody) {
		JSONObject wholePage = new JSONObject(responseBody);
		JSONObject embedded = wholePage.getJSONObject("_embedded");
		JSONArray classifications = embedded.getJSONArray("classifications");
		

		
		HashMap<String, ArrayList<String>> result = new HashMap<>();
		
		for(int i = 0; i < classifications.length(); i++) {
			JSONObject classification = classifications.getJSONObject(i);
			JSONObject type = new JSONObject();
			JSONObject segment = new JSONObject();
			//Array of subtypes/genres
			ArrayList<String> sub = new ArrayList<>();
			try {
				type = classification.getJSONObject("type");
			} catch(JSONException e) {
				//System.out.println("type not found");
			}
			try {
				segment = classification.getJSONObject("segment");
			} catch(JSONException e) {
				//System.out.println("segment not found");
			}
			
			if(!type.isEmpty()) {
				String primary = type.getString("name");
				JSONObject embeddedSubtypes = type.getJSONObject("_embedded");
				JSONArray subtypes = embeddedSubtypes.getJSONArray("subtypes");
				for(int j = 0; j < subtypes.length(); j++) {
					JSONObject subtype = subtypes.getJSONObject(j);
					String subtypeName = subtype.getString("name");
					sub.add(subtypeName);
				}
				result.put(primary, sub);
			}
			else if(!segment.isEmpty()) {
				String primary = segment.getString("name");
				JSONObject embeddedSubgenres = segment.getJSONObject("_embedded");
				JSONArray genres = embeddedSubgenres.getJSONArray("genres");
				for(int j = 0; j < genres.length(); j++) {
					JSONObject genre = genres.getJSONObject(j);
					String genreName = genre.getString("name");
					sub.add(genreName);
				}
				result.put(primary, sub);
			}
			
		}
		
		System.out.println(result.toString());
		
		return result;
		
	}
	
	/*
	 * See ExampleEventsResponse.txt for example response body
	 * Parses events that have been searched
	 */
	public static String parse(String responseBody) {
		JSONObject wholePage = new JSONObject(responseBody);
		JSONObject page = wholePage.getJSONObject("page");
		
		int totalElements = page.getInt("totalElements");
		int totalPages = page.getInt("totalPages");
		int size = page.getInt("size");
		
		if(totalElements == 0) {
			System.out.println("No events found");
			return null;
		}
		
		JSONObject links = wholePage.getJSONObject("_links");
		JSONObject embedded = wholePage.getJSONObject("_embedded");
		
		//Array of events
		JSONArray events = embedded.getJSONArray("events");
		
		
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

