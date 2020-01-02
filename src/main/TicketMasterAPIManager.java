package main;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TicketMasterAPIManager {
	
	private static HttpURLConnection con;
	private static final int MONTHS_FROM_NOW_TO_SEARCH = 1;
	
	/*
	 * Finds events within free TimeIntervals with zipcode and categories
	 */
	public static ArrayList<Event> findAllEvents(String postalCode, ArrayList<TimeInterval> freeTime, ArrayList<String> genres){
		//Look for events up to one month from now
		LocalDateTime start = LocalDateTime.now();
		LocalDateTime end = start.plusMonths(MONTHS_FROM_NOW_TO_SEARCH);
		ArrayList<Event> result = new ArrayList<>();
		ArrayList<Event> temp = new ArrayList<>();
		
		//Get events from now to 1 month from now
		temp = findEvents(postalCode, start, end, genres);
		
		//Get rid of events not in free time
		//Loop through each event
		for(Event e: temp) {
			//If this event is in free time, add to return value result
			if(TimeInterval.isWithin(e, freeTime)) {
				result.add(e);
			}
		}

		return result;
	}

	
	/*
	 * Returns array of Event objects within the start and end time
	 */
	public static ArrayList<Event> findEvents(String postalCode, LocalDateTime start, LocalDateTime end, ArrayList<String> genres) {
		ArrayList<Event> result = new ArrayList<>();
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
			result = parse(responseContent.toString());
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			con.disconnect();
		}
		return result;
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
	private static HashMap<String, ArrayList<String>> parseClassifications(String responseBody) {
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
	 * Parses events that have been searched and returns Array of event objects from TicketMaster
	 */
	private static ArrayList<Event> parse(String responseBody) {
		ArrayList<Event> result = new ArrayList<>();
		JSONObject wholePage = new JSONObject(responseBody);
		
		JSONObject page;
		
		try {
			page = wholePage.getJSONObject("page");
		} catch(JSONException e) {
			//RIP error
			System.out.println(responseBody);
			return result;
		}
		
		int totalElements = page.getInt("totalElements");
		int totalPages = page.getInt("totalPages");
		int size = page.getInt("size");
		
		if(totalElements == 0) {
//			System.out.println("No events found");
			return result;
		}
		
		JSONObject links = wholePage.getJSONObject("_links");
		JSONObject embedded = wholePage.getJSONObject("_embedded");
		
		//Array of events
		JSONArray events = embedded.getJSONArray("events");
		
		
//		System.out.println("Total Elements = " + totalElements);
//		System.out.println("Total Pages = " + totalPages);
//		System.out.println("Elements per page = " + size);
		
		for(int i = 0; i < events.length() ; i++) {
			//Get each event
			JSONObject event = events.getJSONObject(i);
			String name = event.getString("name");
			//Get event id and url to ticketmaster
			String id = event.getString("id");
			String url = event.getString("url");
			//Get an example image
			String firstImage = "No image";
			JSONArray images;
			try {
				images = event.getJSONArray("images");
				firstImage = images.getJSONObject(0).getString("url");
			} catch (JSONException e) {
				
			}
			//Get start DateTime
			JSONObject dates = event.getJSONObject("dates");
			JSONObject start = dates.getJSONObject("start");
			
			
			String startDateTime = null;
			LocalDate tempDate = LocalDate.now();
			LocalTime defaultTime = LocalTime.MIN;
			LocalDateTime eventDateTime = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
			try {
				startDateTime = start.getString("localDate");
				tempDate = LocalDate.parse(startDateTime);
				startDateTime = start.getString("localTime");
				defaultTime = LocalTime.parse(startDateTime);
				eventDateTime = LocalDateTime.of(tempDate, defaultTime);
			} catch(JSONException e) {
				//No specific time
				startDateTime = start.getString("localDate");
				tempDate = LocalDate.parse(startDateTime);
				eventDateTime = LocalDateTime.of(tempDate, defaultTime);
			}
			//Make event object
			Event toSuggest = new Event(name, eventDateTime, id, url, firstImage);
			result.add(toSuggest);
		}
		return result;
	}	
}

