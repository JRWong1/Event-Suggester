package main;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TimeInterval {
	private LocalDateTime start;
	private LocalDateTime end;
	
	/*
	 * Constructor, start time must be before end time
	 */
	public TimeInterval(LocalDateTime start, LocalDateTime end) throws IllegalArgumentException {
		if(!start.isBefore(end)) {
			throw new IllegalArgumentException("start must come before end");
		}
		this.start = start;
		this.end = end;
	}
	
	/*
	 * Takes in a LocalDateTime object and TimeInterval and checks if it is inside the interval
	 * Inclusive at the start
	 * Excludes the last 1 hour because events normally last at least 1 hour
	 */
	public static boolean isWithin(LocalDateTime myTime, TimeInterval interval) {
		//Exclude last 1 hour
		LocalDateTime hourBeforeEnd = interval.getEnd().minusHours(1);
		if(myTime.isEqual(interval.getStart())) {
			return true;
		}
		else if(myTime.isEqual(hourBeforeEnd)) {
			return true;
		}
		else if(interval.getStart().isBefore(myTime) && hourBeforeEnd.isAfter(myTime)) {
			return true;
		}
		return false;
	}
	/*
	 * Takes in event and free time (Array of Time intervals)
	 * Returns true if event is within free time
	 */
	public static boolean isWithin(Event event, ArrayList<TimeInterval> freeTime) {
		LocalDateTime eventTime = event.start;
		//Go through freeTime
		for(TimeInterval t: freeTime) {
			//Event is within this particular interval
			if(TimeInterval.isWithin(eventTime, t)) {
				return true;
			}
		}
		return false;
	}
	
	/*
	 * Reverses the time intervals (gets free time given events)
	 */
	public static ArrayList<TimeInterval> invert(ArrayList<TimeInterval> in) {
		ArrayList<TimeInterval> result = new ArrayList<>();
		
		//Will only check for times within one month
		LocalDateTime start = LocalDateTime.now();
		LocalDateTime end = start.plusMonths(1);
		result.add(new TimeInterval(start, end));
		if(in.isEmpty()) {
			return result;
		}
		for(TimeInterval e: in) {
			splitTime(result, e);
		}
		return result;
	}
	/*
	 * Same as invert
	 */
	public static ArrayList<TimeInterval> getFreeTime(ArrayList<TimeInterval> in) { 
		return invert(in);
	}
	
	/*
	 * Given one time interval, splits up the times accordingly
	 */
	private static void splitTime(ArrayList<TimeInterval> toSplit, TimeInterval event) {
		for(int i = 0; i < toSplit.size(); i++) {
			TimeInterval t = toSplit.get(i);
			//Ignore current interval if it ends before the event begins
			//Also ignore if event ends before current interval begins
			if(t.getEnd().isBefore(event.getStart()) || t.getStart().isAfter(event.getEnd())) {
				continue;
			}
			//Considered outside if equal in this way
			if(t.getEnd().isEqual(event.getStart()) || t.getStart().isEqual(event.getEnd())) {
				continue;
			}
			//Entirely equal
			if(t.getStart().isEqual(event.getStart()) && t.getEnd().isEqual(event.getEnd())) {
				toSplit.remove(i);
				break;
			}
			//Event is bigger than time interval, remove it
			if(t.getStart().isAfter(event.getStart()) && t.getEnd().isBefore(event.getEnd())) {
				toSplit.remove(i);
			}
			//Check for all inside
			if(t.getStart().isBefore(event.getStart()) && t.getEnd().isAfter(event.getEnd())) {
				TimeInterval a = new TimeInterval(t.getStart(), event.getStart());
				TimeInterval b = new TimeInterval(event.getEnd(), t.getEnd());
				//Remove current and replace;
				toSplit.remove(i);
				toSplit.add(i, b);
				toSplit.add(i, a);
				break;
			}
			//outside then inside
			else if(event.getEnd().isAfter(t.getStart())) {
				t.setStart(event.getEnd());
			}
			//inside then outside
			else if(event.getStart().isBefore(t.getEnd())) {
				t.setEnd(event.getStart());
			}
		}
	}
	
	
	public LocalDateTime getStart() {
		return start;
	}
	public LocalDateTime getEnd() {
		return end;
	}
	public void setStart(LocalDateTime start) {
		this.start = start;
	}
	public void setEnd(LocalDateTime end) {
		this.end = end;
	}
	
	public String toString() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
		String startString = start.format(formatter);
		String endString = end.format(formatter);
		return "from	" + startString + "   to   " + endString;
	}
	
	
	
}
