package app;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class EventController {

	//Returns an ArrayList of events
  @RequestMapping("/suggest") //@RequestMaping(method=GET)
  public ArrayList<Event> suggest(@RequestParam(value="postalCode", defaultValue="08901") String postalCode, @RequestParam(value="genres", defaultValue="music") ArrayList<String> genres) {
		ArrayList<TimeInterval> events = new ArrayList<>();
		ArrayList<TimeInterval> freeTime = new ArrayList<>();
		ArrayList<Event> suggestedEvents = new ArrayList<>();
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
		suggestedEvents = TicketMasterAPIManager.findAllEvents(postalCode, freeTime, genres);
		if(suggestedEvents.isEmpty()) {
			System.out.println("No events found");
		}else {
			System.out.println(suggestedEvents.toString());
		}
		return suggestedEvents;
  }
}