package main;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TimeInterval {
	private LocalDateTime start;
	private LocalDateTime end;
	
	
	public TimeInterval(LocalDateTime start, LocalDateTime end) throws IllegalArgumentException {
		if(!start.isBefore(end)) {
			throw new IllegalArgumentException("start must come before end");
		}
		this.start = start;
		this.end = end;
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
