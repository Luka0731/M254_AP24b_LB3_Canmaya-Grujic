package ch.concertticketwatcherengine.component.service;

import ch.concertticketwatcherengine.component.fetcher.GeolocationFetcher;
import ch.concertticketwatcherengine.component.fetcher.TicketmasterEventFetcher;
import ch.concertticketwatcherengine.component.model.Event;
import ch.concertticketwatcherengine.component.model.Location;
import ch.concertticketwatcherengine.component.repository.EventWatchRepository;
import ch.concertticketwatcherengine.core.exception.DatabaseException;
import ch.concertticketwatcherengine.core.generic.Service;
import ch.concertticketwatcherengine.core.util.Log;
import ch.concertticketwatcherengine.core.util.ThreadUtil;
import java.util.HashMap;
import java.util.Map;

public class ConcertTaskService extends Service {

    private static final long CHECK_INTERVAL_HOURS = 12;
    private static final long MAX_WAIT_YEARS = 50;
    private static final long MAX_CHECKS = MAX_WAIT_YEARS * ThreadUtil.DAYS_IN_YEAR * ThreadUtil.HOURS_IN_DAY / CHECK_INTERVAL_HOURS;
    private final GeolocationFetcher geolocationFetcher;
    private final TicketmasterEventFetcher eventFetcher;
    private final EventWatchRepository eventWatchRepository;

    public ConcertTaskService(GeolocationFetcher geolocationFetcher, TicketmasterEventFetcher eventFetcher, EventWatchRepository eventWatchRepository) {
        this.geolocationFetcher = geolocationFetcher;
        this.eventFetcher = eventFetcher;
        this.eventWatchRepository = eventWatchRepository;
    }

    @Override
    public void execute() {
        String processInstanceId = (String) receivedData.getOrDefault("processInstanceId", "");
        String artistName = readArtistName();
        String maxDistanceKm = readMaxDistance();
        Location location = fetchUserLocation();

        Log.debug("{ConcertTaskService} Starting watch for: " + artistName + " | distance: " + maxDistanceKm + "km | location: " + location.getLatitude() + "," + location.getLongitude());

        for (long attempt = 0; attempt < MAX_CHECKS; attempt++) {
            Log.debug("{ConcertTaskService} Poll attempt " + (attempt + 1) + " of " + MAX_CHECKS);
            Event event = findNewEvent(processInstanceId, artistName, maxDistanceKm, location);
            if (event != null) {
                Log.debug("{ConcertTaskService} New event found: " + event.getName() + " | id: " + event.getId());
                putEventIntoReturnData(event);
                return;
            }
            Log.debug("{ConcertTaskService} No new event found. Sleeping " + CHECK_INTERVAL_HOURS + "h before next attempt.");
            if (attempt < MAX_CHECKS - 1) sleep();
        }

        throw new RuntimeException("{ConcertTaskService} Watcher timed out after " + MAX_WAIT_YEARS + " years for: " + artistName);
    }



    // |----- helper methods -----|

    private String readArtistName() {
        String name = (String) receivedData.getOrDefault("artistName", "");
        if (name.isBlank()) throw new RuntimeException("{ConcertTaskService} artistName cannot be empty");
        return name;
    }

    private String readMaxDistance() {
        return String.valueOf(receivedData.getOrDefault("maxDistanceKm", "100"));
    }

    private Location fetchUserLocation() {
        try {
            Location location = geolocationFetcher.fetch(new HashMap<>());
            if (location == null) throw new RuntimeException("{ConcertTaskService} Geolocation returned null");
            return location;
        } catch (Exception e) {
            throw new RuntimeException("{ConcertTaskService} Geolocation failed — aborting: " + e.getMessage());
        }
    }

    private Event findNewEvent(String processInstanceId, String artistName, String maxDistanceKm, Location location) {
        try {
            Map<String, String> filters = new HashMap<>();
            filters.put("artistName", artistName);
            filters.put("latlong", location.getLatitude() + "," + location.getLongitude());
            filters.put("radius", maxDistanceKm);

            Event event = eventFetcher.fetch(filters);

            if (event == null) {
                Log.debug("{ConcertTaskService} Ticketmaster returned no events");
                return null;
            }

            Log.debug("{ConcertTaskService} Event found: " + event.getName() + " | id: " + event.getId());

            if (eventWatchRepository.hasSeenEvent(processInstanceId, event.getId())) {
                Log.debug("{ConcertTaskService} Event already seen for this watcher, skipping: " + event.getId());
                return null;
            }

            eventWatchRepository.markEventAsSeen(processInstanceId, event.getId());
            return event;

        } catch (DatabaseException e) {
            throw new RuntimeException("{ConcertTaskService} DB error — aborting: " + e.getMessage());
        } catch (Exception e) {
            Log.error("{ConcertTaskService} Fetch error, retrying next cycle: " + e.getMessage());
            return null;
        }
    }

    private void putEventIntoReturnData(Event event) {
        returnData.put("eventName", event.getName());
        returnData.put("eventVenue", event.getVenue());
        returnData.put("eventCity", event.getCity());
        returnData.put("eventDate", event.getDate());
        returnData.put("eventPrice", event.getPriceMin());
        returnData.put("eventUrl", event.getUrl());
        returnData.put("eventImageUrl", event.getImageUrl());
        returnData.put("eventId", event.getId());
    }

    private void sleep() {
        ThreadUtil.sleepHours(CHECK_INTERVAL_HOURS);
    }
}