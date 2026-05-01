package ch.concertticketwatcherengine.component.fetcher;

import ch.concertticketwatcherengine.component.model.Event;
import ch.concertticketwatcherengine.core.generic.Fetcher;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class TicketmasterEventFetcher extends Fetcher<Event> {

    private static final String API_KEY = "s76hXfKVflEt8ARCNzzSsksm8MZItF3A";

    @Override
    protected String buildUrl(Map<String, String> filters) {
        String artist = filters.getOrDefault("artistName", "").replace(" ", "%20");
        String latlong = filters.getOrDefault("latlong", "");
        String radius  = filters.getOrDefault("radius", "50");

        return "https://app.ticketmaster.com/discovery/v2/events.json"
                + "?apikey=" + API_KEY
                + "&keyword=" + artist
                + "&classificationName=music"
                + "&latlong=" + latlong
                + "&radius=" + radius
                + "&unit=km"
                + "&sort=date,asc"
                + "&size=1";
    }

    @Override
    protected Event mapResponseJsonToModel(StringBuilder response) throws Exception {
        JSONObject root = new JSONObject(response.toString());

        JSONObject embedded = root.optJSONObject("_embedded");
        if (embedded == null) return null;

        JSONArray events = embedded.optJSONArray("events");
        if (events == null || events.isEmpty()) return null;

        JSONObject e = events.getJSONObject(0);

        Event event = new Event();
        event.id   = e.optString("id");
        event.name = e.optString("name");
        event.url  = e.optString("url");

        // Image
        JSONArray images = e.optJSONArray("images");
        if (images != null && !images.isEmpty()) {
            event.imageUrl = images.getJSONObject(0).optString("url");
        }

        // Date
        JSONObject dates = e.optJSONObject("dates");
        if (dates != null) {
            JSONObject start = dates.optJSONObject("start");
            if (start != null) event.date = start.optString("dateTime", start.optString("localDate"));
        }

        // Venue
        JSONObject embeddedInner = e.optJSONObject("_embedded");
        if (embeddedInner != null) {
            JSONArray venues = embeddedInner.optJSONArray("venues");
            if (venues != null && !venues.isEmpty()) {
                JSONObject v = venues.getJSONObject(0);
                event.venue = v.optString("name");
                event.city  = v.optJSONObject("city") != null
                        ? v.getJSONObject("city").optString("name") : "";

                JSONObject loc = v.optJSONObject("location");
                if (loc != null) {
                    event.latitude  = Double.parseDouble(loc.optString("latitude", "0"));
                    event.longitude = Double.parseDouble(loc.optString("longitude", "0"));
                }
            }
        }

        // Price
        JSONArray priceRanges = e.optJSONArray("priceRanges");
        if (priceRanges != null && !priceRanges.isEmpty()) {
            event.priceMin = String.valueOf(priceRanges.getJSONObject(0).optDouble("min", 0));
        } else {
            event.priceMin = "N/A";
        }

        return event;
    }
}