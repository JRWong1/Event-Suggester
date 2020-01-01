package main;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import main.java.GoogleCalendarAPIManager;

public class Driver {

	public static void main(String[] args) {
		//zipcode and genres to search
		String postalCode = "08901";
		String[] genres = new String[] {"music"};
		
		ArrayList<TimeInterval> events = new ArrayList<>();
		ArrayList<TimeInterval> freeTime = new ArrayList<>();
		//Get my google calendar events
		try {
			events = GoogleCalendarAPIManager.getEvents();
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}
		//Find my free time
		freeTime = TimeInterval.getFreeTime(events);
		
		System.out.println(events.toString());
		System.out.println();
		System.out.println("Free Time");
		System.out.println(freeTime.toString());
		
		//Suggest Events and print
		ArrayList<Event> suggestedEvents = new ArrayList<>();
		suggestedEvents = TicketMasterAPIManager.findAllEvents(postalCode, freeTime, genres);
		if(suggestedEvents.isEmpty()) {
			System.out.println("No events found");
		}else {
			System.out.println(suggestedEvents.toString());
		}
	}
	
}
