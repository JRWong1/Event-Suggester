package main;

import java.time.LocalDateTime;

public class Event {
	public String name;
	public LocalDateTime start;
	public String id;
	public String link;
	public String imageLink;
	
	
	public Event(String name, LocalDateTime start, String id, String link, String imageLink) {
		this.name = name;
		this.start = start;
		this.id = id;
		this.imageLink = imageLink;
		this.link = link;
	}
	public String toString(){
		String result = "Event - " + name + "\n";
		result += "		Begins: " + start + "\n";
		result += "		Event ID: " + id + "\n";
		result += "		Ticket Master link: " + link + "\n";
		result += "		Image url: " + imageLink + "\n";

		return result;
	}
}