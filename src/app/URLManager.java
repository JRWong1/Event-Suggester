package app;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class URLManager {
	
	private static String TicketMasterBase = "https://app.ticketmaster.com/discovery/v2/";
	private static String TicketMasterKey = "sZvd7rdheL5ZlSnIw3YIvQGGv5uoqEqh";
	
	
	public static String classificationsURL() {
		String result = TicketMasterBase + "classifications.json?";
		result += "apikey=" + TicketMasterKey;
		return result;
	}
	
	/*
	 * Searches events by postal code and start end local time.
	 * Includes classification for category of interest such as music
	 * Defaults to postalCode of 08901
	 */
	public static String searchEventURL(String postalCode, LocalDateTime startDateTime, LocalDateTime endDateTime, String[] classificationName ) {
		//Default country is already US, default locale is "en"
		
		String result = TicketMasterBase + "events.json?";
		if(postalCode == null) {
			//Default to Rutgers
			result += "postalCode=08901";
		} else {
			result += "postalCode=" + postalCode;
		}
		
		
		//Example 2020-10-11T11:01:00Z works
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss ");
		
		if(startDateTime != null) {
			String formattedDate = startDateTime.format(format);
			formattedDate = formattedDate.replaceFirst(" ", "T");
			formattedDate = formattedDate.replaceFirst(" ", "Z");
			result += "&startDateTime=" + formattedDate;
		}
		if(endDateTime != null) {
			String formattedDate = endDateTime.format(format);
			formattedDate = formattedDate.replaceFirst(" ", "T");
			formattedDate = formattedDate.replaceFirst(" ", "Z");
			result += "&endDateTime=" + formattedDate;
		}
		
		if(classificationName != null) {
			result += "&" + "classificationName=";
			for(int i = 0; i < classificationName.length; i++) {
				if(i == 0) {
					result += classificationName[i];
				}
				else {
					result += "," + classificationName[i];
				}
			}
		}

			
		String sort = "date,asc";
		result += "&sort=" + sort;
		
		result += "&apikey=" + TicketMasterKey;
		return result;
	}
	
}
