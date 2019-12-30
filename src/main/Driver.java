package main;

import java.time.LocalDateTime;

public class Driver {

	public static void main(String[] args) {
		//Try today's date
		LocalDateTime start = LocalDateTime.now();
		//End 1 month after
		LocalDateTime end = start.plusMonths(1);
		String postalCode = "08901";
		String[] genres = new String[] {"music"};
		
		TicketMasterAPIManager.findEvents(postalCode, start, end, genres);
		
		TicketMasterAPIManager.getClassifications();
	}
	
}
