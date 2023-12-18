package edu.ucsb.cs156.example.controllers;

import java.io.FileInputStream;
import java.util.Collections;
import java.util.Date;

import edu.ucsb.cs156.example.repositories.GCalRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.ucsb.cs156.example.entities.GCal;

import com.google.api.services.calendar.model.Event;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar.Events;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventAttendee;
import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.AclRule;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

//import com.google.api.services.calendar.model.Scope;


import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import edu.ucsb.cs156.example.services.GoogleTokenService;
import com.google.api.client.http.javanet.NetHttpTransport;

import java.util.Arrays;



import java.util.stream.Collectors;
import java.util.List;

import javax.validation.Valid;

@Tag(name = "GCalController")
@RequestMapping("/api/gcal")
@Controller
@Slf4j
public class GCalController{
        //adjusted to read for summary since i am storing email there
        private boolean containsAttendeeEmail1(Event event, String driverEmail){
        List<EventAttendee> attendees = event.getAttendees();
        if(attendees == null){
                return false;
        }
        for(int i = 0; i < attendees.size(); i++){
                if(!attendees.get(i).getEmail().equals(driverEmail)){
                        return false;
                }
        }
        return true;
        }
    private boolean containsAttendeeEmail(Event event, String driverEmail){
        //List<EventAttendee> attendees = event.getAttendees();
        // if(attendees == null){
        //         return false;
        // }
        // for(int i = 0; i < attendees.size(); i++){
        //         if(attendees.get(i).getEmail() != driverEmail){
        //                 return false;
        //         }
        // }
        // return true;
        String email = event.getSummary();
       /// System.out.println("Our test " + email);
        //System.out.println("Our param " + driverEmail);
        if(!driverEmail.equals(email)){
                System.out.println("Our test " + email);
                System.out.println("Our param " + driverEmail);
                return false;
        }
        System.out.println("making sure we return true once");
        return true;

    }
    @Autowired
    GoogleTokenService googleTokenService;

    @Autowired
    GCalRepository gCalRepository;

    @Value("${app.gcal.calendarId:primary}")
    private String calendarId;

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";

    @Operation(summary = "Get events from Google Calendar", description = "Get events from Google Calendar")
    @GetMapping(value = "/events")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<String> getAllEvents() throws Exception {
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream("credentials.json"))
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/calendar"));

        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service = new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME).build();
        Events events = service.events();
        // log.info("events={}", events);
        com.google.api.services.calendar.model.Events eventList = events.list(calendarId)
                .setTimeZone("America/Los_Angeles")
                .execute();
        log.info("eventList={}", eventList.toPrettyString());
        return new ResponseEntity<String>(eventList.toPrettyString(), HttpStatus.OK);
    }

     @Operation(summary = "Get events from Google Calendar", description = "Get events from Google Calendar")
    @GetMapping(value = "/events/bydate")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<String> getEventsByDate(
        @Parameter(description="Start Date", required=true)
        @RequestParam("sdate") String sdate,
        @Parameter(description="End Date", required=true)
        @RequestParam("edate") String edate
        ) throws Exception {
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream("credentials.json"))
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/calendar"));

        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service = new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME).build();
        Events events = service.events();
        // log.info("events={}", events);

        final DateTime date1 = new DateTime(sdate + "T00:00:00");
        final DateTime date2 = new DateTime(edate + "T23:59:59");

        com.google.api.services.calendar.model.Events eventList = events.list(calendarId)
                .setTimeZone("America/Los_Angeles")
                .setTimeMin(date1).setTimeMax(date2)
                .execute();
        log.info("eventList={}", eventList.toPrettyString());
        return new ResponseEntity<String>(eventList.toPrettyString(), HttpStatus.OK);
    }



    @Operation(summary="Post a shift on Google Calendar", description="Post a shift on Google Calendar")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/new")
    public void newShift(@Parameter(name = "summary") @RequestParam String summary,
    @Parameter(name = "location") @RequestParam String location,
    @Parameter(name = "description") @RequestParam String description,
    @Parameter(name = "sDate") @RequestParam String sDate,
    @Parameter(name = "eDate") @RequestParam String eDate,
    //@Parameter(name = "driverEmail") @RequestParam String driverEmail,
    @Parameter(name = "endTime") @RequestParam String endTime,
    @Parameter(name = "startTime") @RequestParam String startTime
    ) throws Exception {
        //maybe we should still store in the repo and return the Gcal entity object so we dont really run into errors
        //probably easier if to test as well
        Event googleCalendarEvent = new Event();
        //final DateTime date1 = new DateTime(sDate + "T00:00:00");
        final DateTime date1 = new DateTime(sDate + "T" + startTime + "-07:00");
        //final DateTime date2 = new DateTime(eDate + "T23:59:59");
        final DateTime date2 = new DateTime(eDate + "T" + endTime + "-07:00");
        googleCalendarEvent.setSummary(summary);
        googleCalendarEvent.setDescription(description);
        googleCalendarEvent.setLocation(location);
        EventDateTime start = new EventDateTime()
                .setDateTime(date1)
                .setTimeZone("America/Los_Angeles");
                googleCalendarEvent.setStart(start);

        EventDateTime end = new EventDateTime()
                .setDateTime(date2)
                .setTimeZone("America/Los_Angeles");
                googleCalendarEvent.setEnd(end);

        // EventAttendee[] attendees = new EventAttendee[] {
        //         new EventAttendee().setEmail(driverEmail)
        // };
        // googleCalendarEvent.setAttendees(Arrays.asList(attendees));

        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream("credentials.json"))
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/calendar"));

        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        Calendar service = new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME).build();

       
        googleCalendarEvent = service.events().insert(calendarId, googleCalendarEvent).execute();
    }

    @Operation(summary="Return all personal shifts", description = "Return all personal shifts")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = "/personalshifts")
    public ResponseEntity<String> getPersonalShifts (@Parameter(name = "driverEmail") @RequestParam String driverEmail
    ) throws Exception {
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream("credentials.json"))
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/calendar"));

        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service = new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME).build();

         Events events = service.events();

        com.google.api.services.calendar.model.Events eventList = events.list(calendarId)
                .setTimeZone("America/Los_Angeles")
                .execute();

        List<Event> events2 = eventList.getItems();

        // List<Event> filteredEvents = events.stream()
        //         .filter(event -> containsAttendeeEmail(event, driverEmail))
        //         .collect(Collectors.toList());
         List<Event> filteredEvents = events2.stream()
                     .filter(event -> containsAttendeeEmail1(event, driverEmail))
                     .collect(Collectors.toList());
        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(filteredEvents);

        return ResponseEntity.ok().body(body);


    }
    @Operation(summary="Grant access to calendar")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/grant")
    public String grantAcess(@Parameter(name="driverEmail") @RequestParam String driverEmail)
    throws Exception{
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream("credentials.json"))
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/calendar"));

        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service = new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME).build();

        AclRule rule = new AclRule();
        //Scope scope = new Scope();
        //scope.setType("user").setValue(driverEmail);
        //rule.setScope(driverEmail);
        rule.setRole("reader");
        rule.setScope(new AclRule.Scope().setType("user").setValue(driverEmail));

        AclRule createdRule = service.acl().insert(calendarId, rule).execute();
        return driverEmail;
    }

     


}