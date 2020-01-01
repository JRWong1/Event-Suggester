package main;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import main.java.GoogleCalendarAPIManager;

public class Driver {

	public static void main(String[] args) {
//		//Try today's date
//		LocalDateTime start = LocalDateTime.now();
//		//End 1 month after
//		LocalDateTime end = start.plusMonths(1);
//		String postalCode = "08901";
//		String[] genres = new String[] {"music"};
//		
//		TicketMasterAPIManager.findEvents(postalCode, start, end, genres);
//		
//		TicketMasterAPIManager.getClassifications();
		
		//Try Google Calendar API
		ArrayList<TimeInterval> events = new ArrayList<>();
		try {
			events = GoogleCalendarAPIManager.getEvents();
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}
		
		System.out.println(events.toString());
		
	}
	
}
