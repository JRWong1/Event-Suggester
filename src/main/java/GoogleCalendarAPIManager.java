package main.java;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import main.TimeInterval;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GoogleCalendarAPIManager {
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "../resources/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GoogleCalendarAPIManager.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
    
    
    /*
     * Gets first 50 events from Google Calendars and returns an ArrayList of time intervals of those events
     * Need to add calendars into your Google Calendar first
     */
    public static ArrayList<TimeInterval> getEvents() throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        
        DateTime now = new DateTime(System.currentTimeMillis());
        List<Event> items = null;
        

        String pageToken = null;
        do {
        	//Get each calendar in group, watch out for extraneous calendars such as "Birthdays," although this may still be relevant
          CalendarList calendarList = service.calendarList().list().setPageToken(pageToken).execute();
          List<CalendarListEntry> calendars = calendarList.getItems();
          ArrayList<String> calendarIds = new ArrayList<>();

          for (CalendarListEntry calendarListEntry : calendars) {
        	  //get events from each person in group
        	  String id = calendarListEntry.getId();
        	  Events temp = service.events().list(id)
                      .setMaxResults(50)
                      .setTimeMin(now)
                      .setOrderBy("startTime")
                      .setSingleEvents(true)
                      .execute();
        	  if(items == null) {
        		  items = temp.getItems();
        	  }
        	  else {
        		  items.addAll(temp.getItems());
        	  }
          }
          pageToken = calendarList.getNextPageToken();
        } while (pageToken != null);
        
        
        ArrayList<TimeInterval> eventsAL= new ArrayList<>();
        if (items.isEmpty()) {
            System.out.println("No upcoming events found.");
        } else {
            System.out.println("Upcoming events");
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                DateTime end = event.getEnd().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                if(end == null) {
                	end = event.getEnd().getDate();
                }
                TimeInterval myEvent = new TimeInterval(GoogleCalendarAPIManager.convertLocalDateTime(start), GoogleCalendarAPIManager.convertLocalDateTime(end));
                eventsAL.add(myEvent);
            }
        }
        return eventsAL;
    }
    

    
    /*
     * Converts a Google DateTime object to java LocalDateTime object
     */
    private static LocalDateTime convertLocalDateTime(DateTime event) {
    	LocalDateTime result = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getValue()), ZoneId.systemDefault());
    	return result;
    }
}
