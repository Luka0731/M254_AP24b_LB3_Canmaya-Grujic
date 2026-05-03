package ch.concertticketwatcherengine.component.fetcher;

import ch.concertticketwatcherengine.component.model.Ticket;
import ch.concertticketwatcherengine.core.exception.TicketmasterApiException;
import ch.concertticketwatcherengine.core.generic.Fetcher;
import ch.concertticketwatcherengine.core.setup.Config;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Map;

public class TicketmasterTicketFetcher extends Fetcher<Ticket> {

    private static final String API_KEY = Config.get("ticketmaster.api.key");

    @Override
    protected String buildUrl(Map<String, String> filters) {
        String eventId = filters.getOrDefault("eventId", "");
        return "https://app.ticketmaster.com/discovery/v2/events/" + eventId + ".json"
                + "?apikey=" + API_KEY;
    }

    @Override
    protected Ticket mapResponseJsonToModel(StringBuilder response) throws TicketmasterApiException {
        try {
            JSONObject root = new JSONObject(response.toString());
            Ticket ticket = new Ticket();
            ticket.setUrl(root.optString("url"));

            // |----- availability status -----|
            JSONObject dates = root.optJSONObject("dates");
            if (dates != null) {
                JSONObject status = dates.optJSONObject("status");
                if (status != null) {
                    String code = status.optString("code", "");
                    ticket.setAvailable(code.equals("onsale") || code.equals("rescheduled"));
                }

                // |----- presale date -----|
                JSONObject sales = root.optJSONObject("sales");
                if (sales != null) {
                    JSONObject publicSale = sales.optJSONObject("public");
                    if (publicSale != null) {
                        String startDateTime = publicSale.optString("startDateTime", "");
                        if (!startDateTime.isBlank()) {
                            ticket.setPresaleDate(startDateTime);
                        }
                    }
                }
            }

            // |----- price -----|
            JSONArray priceRanges = root.optJSONArray("priceRanges");
            if (priceRanges != null && !priceRanges.isEmpty()) {
                double min      = priceRanges.getJSONObject(0).optDouble("min", 0);
                String currency = priceRanges.getJSONObject(0).optString("currency", "CHF");
                ticket.setPrice(min + " " + currency);
            } else {
                ticket.setPrice("N/A");
            }

            return ticket;
        } catch (Exception ex) {
            throw new TicketmasterApiException("Failed to parse ticket response: " + ex.getMessage());
        }
    }
}