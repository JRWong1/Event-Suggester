package main;

import java.time.LocalDateTime;

public class TimeInterval {
	private LocalDateTime start;
	private LocalDateTime end;
	
	
	public TimeInterval(LocalDateTime start, LocalDateTime end) {
		this.start = start;
		this.end = end;
	}
	
	public LocalDateTime getStart() {
		return start;
	}
	public LocalDateTime getEnd() {
		return end;
	}
	
	public String toString() {
		return start.toString() + "|||" + end.toString();
	}
}
