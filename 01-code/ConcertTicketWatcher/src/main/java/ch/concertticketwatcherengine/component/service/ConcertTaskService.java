package ch.concertticketwatcherengine.component.service;

import ch.concertticketwatcherengine.component.fetcher.GeolocationFetcher;
import ch.concertticketwatcherengine.component.fetcher.TicketmasterEventFetcher;
import ch.concertticketwatcherengine.component.model.Event;
import ch.concertticketwatcherengine.component.model.Location;
import ch.concertticketwatcherengine.core.generic.Service;

import java.util.HashMap;
import java.util.Map;

public class ConcertTaskService extends Service {

    private final GeolocationFetcher geolocationFetcher;
    private final TicketmasterEventFetcher eventFetcher;

    public ConcertTaskService(GeolocationFetcher geolocationFetcher,
                              TicketmasterEventFetcher eventFetcher) {
        this.geolocationFetcher = geolocationFetcher;
        this.eventFetcher = eventFetcher;
    }

    @Override
    public void execute() {
        Object artistObj = receivedData.get("artistName");
        String artistName = (artistObj == null) ? "" : artistObj.toString();
        if (artistName.isBlank()) {
            throw new RuntimeException("artistName cannot be empty");
        }
        String maxDistanceKm = String.valueOf(receivedData.getOrDefault("maxDistanceKm", "100"));
        String userName      = (String) receivedData.get("userName");

        System.out.println("[ConcertTaskService] Watching for: " + artistName + " (user: " + userName + ")");

        try {
            // 1. Get user's location from their IP
            Location location = geolocationFetcher.fetch(new HashMap<>());
            if (location == null) {
                throw new Exception("Could not determine user location");
            }
            String latlong = location.latitude + "," + location.longitude;
            System.out.println("[ConcertTaskService] User location: " + location.city + " (" + latlong + ")");

            // 2. Search Ticketmaster for events near the user
            Map<String, String> filters = new HashMap<>();
            filters.put("artistName", artistName);
            filters.put("latlong", latlong);
            filters.put("radius", maxDistanceKm);

            Event event = eventFetcher.fetch(filters);

            if (event == null) {
                // No concert found — throw so Camunda keeps the token waiting
                throw new Exception("No concert found yet for: " + artistName);
            }

            System.out.println("[ConcertTaskService] Concert found: " + event.name + " @ " + event.venue);

            // 3. Put event data into return variables for Camunda
            returnData.put("eventName",     event.name);
            returnData.put("eventVenue",    event.venue);
            returnData.put("eventCity",     event.city);
            returnData.put("eventDate",     event.date);
            returnData.put("eventPrice",    event.priceMin);
            returnData.put("eventUrl",      event.url);
            returnData.put("eventImageUrl", event.imageUrl);
            returnData.put("eventId",       event.id);

        } catch (Exception e) {
            System.out.println("[ConcertTaskService] ERROR: " + e.getMessage());
            // Re-throw so Handler can report failure back to Camunda
            throw new RuntimeException(e.getMessage());
        }
    }
}