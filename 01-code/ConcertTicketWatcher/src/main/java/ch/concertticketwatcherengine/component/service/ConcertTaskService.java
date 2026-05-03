package ch.concertticketwatcherengine.component.service;

import ch.concertticketwatcherengine.component.fetcher.GeolocationFetcher;
import ch.concertticketwatcherengine.component.fetcher.TicketmasterEventFetcher;
import ch.concertticketwatcherengine.component.model.Event;
import ch.concertticketwatcherengine.component.model.Location;
import ch.concertticketwatcherengine.component.repository.EventWatchRepository;
import ch.concertticketwatcherengine.core.exception.DatabaseException;
import ch.concertticketwatcherengine.core.generic.Service;
import ch.concertticketwatcherengine.core.util.Log;
import java.util.HashMap;
import java.util.Map;
import ch.concertticketwatcherengine.core.util.ThreadUtil;

public class ConcertTaskService extends Service {

    private static final long CHECK_INTERVAL_HOURS = 12;
    private static final long MAX_WAIT_YEARS = 50;
    private static final long MAX_CHECKS = MAX_WAIT_YEARS * ThreadUtil.DAYS_IN_YEAR * ThreadUtil.HOURS_IN_DAY / CHECK_INTERVAL_HOURS;
    private final GeolocationFetcher geolocationFetcher;
    private final TicketmasterEventFetcher eventFetcher;
    private final EventWatchRepository eventWatchRepository;

    public ConcertTaskService(GeolocationFetcher geolocationFetcher, TicketmasterEventFetcher eventFetcher, EventWatchRepository eventWatchRepository) {
        this.geolocationFetcher   = geolocationFetcher;
        this.eventFetcher         = eventFetcher;
        this.eventWatchRepository = eventWatchRepository;
    }

    @Override
    public void execute() {
        String   artistName    = readArtistName();
        String   maxDistanceKm = readMaxDistance();
        Location location      = fetchUserLocation();

        for (long attempt = 0; attempt < MAX_CHECKS; attempt++) {
            Event event = findNewEvent(artistName, maxDistanceKm, location);
            if (event != null) {
                putEventIntoReturnData(event);
                return;
            }
            sleep();
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

    private Event findNewEvent(String artistName, String maxDistanceKm, Location location) {
        try {
            Map<String, String> filters = new HashMap<>();
            filters.put("artistName", artistName);
            filters.put("latlong", location.getLatitude() + "," + location.getLongitude());
            filters.put("radius", maxDistanceKm);

            Event event = eventFetcher.fetch(filters);
            if (event == null) return null;

            if (eventWatchRepository.hasSeenEvent(event.getId())) return null;

            eventWatchRepository.markEventAsSeen(event.getId());
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