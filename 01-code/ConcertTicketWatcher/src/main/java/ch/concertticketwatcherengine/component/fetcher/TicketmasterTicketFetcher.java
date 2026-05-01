package ch.concertticketwatcherengine.component.fetcher;

import ch.concertticketwatcherengine.component.model.Ticket;
import ch.concertticketwatcherengine.core.generic.Fetcher;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class TicketmasterTicketFetcher extends Fetcher<Ticket> {

    private static final String API_KEY = "s76hXfKVflEt8ARCNzzSsksm8MZItF3A"; // actual api_key

    @Override
    protected String buildUrl(Map<String, String> filters) {
        String eventId = filters.getOrDefault("eventId", "");
        return "https://app.ticketmaster.com/discovery/v2/events/" + eventId + ".json"
                + "?apikey=" + API_KEY;
    }

    @Override
    protected Ticket mapResponseJsonToModel(StringBuilder response) throws Exception {
        JSONObject root = new JSONObject(response.toString());

        Ticket ticket = new Ticket();
        ticket.url = root.optString("url");

        // Check if tickets are on sale
        JSONObject dates = root.optJSONObject("dates");
        if (dates != null) {
            JSONObject status = dates.optJSONObject("status");
            if (status != null) {
                String code = status.optString("code", "");
                ticket.available = code.equals("onsale") || code.equals("rescheduled");
            }
        }

        // Get price
        JSONArray priceRanges = root.optJSONArray("priceRanges");
        if (priceRanges != null && !priceRanges.isEmpty()) {
            double min = priceRanges.getJSONObject(0).optDouble("min", 0);
            String currency = priceRanges.getJSONObject(0).optString("currency", "CHF");
            ticket.price = min + " " + currency;
        } else {
            ticket.price = "N/A";
        }

        return ticket;
    }
}