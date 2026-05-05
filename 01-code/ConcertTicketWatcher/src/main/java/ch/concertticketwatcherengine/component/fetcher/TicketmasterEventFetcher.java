package ch.concertticketwatcherengine.component.fetcher;

import ch.concertticketwatcherengine.component.model.Event;
import ch.concertticketwatcherengine.core.exception.TicketmasterApiException;
import ch.concertticketwatcherengine.core.generic.Fetcher;
import ch.concertticketwatcherengine.core.setup.Config;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Map;

public class TicketmasterEventFetcher extends Fetcher<Event> {

    private static final String API_KEY = Config.get("ticketmaster.api.key");

    @Override
    protected String buildUrl(Map<String, String> filters) {
        String artist  = filters.getOrDefault("artistName", "").replace(" ", "%20");
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
    protected Event mapResponseJsonToModel(StringBuilder response) throws TicketmasterApiException {
        try {
            JSONObject root = new JSONObject(response.toString());

            JSONObject embedded = root.optJSONObject("_embedded");
            if (embedded == null) return null;

            JSONArray events = embedded.optJSONArray("events");
            if (events == null || events.isEmpty()) return null;

            JSONObject e = events.getJSONObject(0);
            Event event = new Event();

            // |----- basic fields -----|
            event.setId(e.optString("id"));
            event.setName(e.optString("name"));
            event.setUrl(e.optString("url"));

            // |----- image -----|
            JSONArray images = e.optJSONArray("images");
            if (images != null && !images.isEmpty()) {
                event.setImageUrl(images.getJSONObject(0).optString("url"));
            }

            // |----- date -----|
            JSONObject dates = e.optJSONObject("dates");
            if (dates != null) {
                JSONObject start = dates.optJSONObject("start");
                if (start != null) event.setDate(start.optString("dateTime", start.optString("localDate")));
            }

            // |----- venue -----|
            JSONObject embeddedInner = e.optJSONObject("_embedded");
            if (embeddedInner != null) {
                JSONArray venues = embeddedInner.optJSONArray("venues");
                if (venues != null && !venues.isEmpty()) {
                    JSONObject v = venues.getJSONObject(0);

                    String venueName = v.optString("name", "").trim();
                    if (venueName.isBlank()) {
                        JSONObject address = v.optJSONObject("address");
                        venueName = address != null ? address.optString("line1", "") : "";
                    }
                    event.setVenue(venueName);

                    JSONObject city = v.optJSONObject("city");
                    event.setCity(city != null ? city.optString("name", "") : "");

                    JSONObject loc = v.optJSONObject("location");
                    if (loc != null) {
                        event.setLatitude(Double.parseDouble(loc.optString("latitude", "0")));
                        event.setLongitude(Double.parseDouble(loc.optString("longitude", "0")));
                    }
                }
            }

            // |----- price -----|
            JSONArray priceRanges = e.optJSONArray("priceRanges");
            if (priceRanges != null && !priceRanges.isEmpty()) {
                double min      = priceRanges.getJSONObject(0).optDouble("min", 0);
                String currency = priceRanges.getJSONObject(0).optString("currency", "CHF");
                event.setPriceMin(min + " " + currency);
            } else {
                event.setPriceMin("See Ticketmaster");
            }

            return event;

        } catch (Exception ex) {
            throw new TicketmasterApiException("Failed to parse event response: " + ex.getMessage());
        }
    }
}