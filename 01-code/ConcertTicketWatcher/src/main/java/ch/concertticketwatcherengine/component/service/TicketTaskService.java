package ch.concertticketwatcherengine.component.service;

import ch.concertticketwatcherengine.component.fetcher.TicketmasterTicketFetcher;
import ch.concertticketwatcherengine.component.model.Ticket;
import ch.concertticketwatcherengine.core.generic.Service;

import java.util.HashMap;
import java.util.Map;

public class TicketTaskService extends Service {

    private final TicketmasterTicketFetcher ticketFetcher;

    public TicketTaskService(TicketmasterTicketFetcher ticketFetcher) {
        this.ticketFetcher = ticketFetcher;
    }

    @Override
    public void execute() {
        String eventId = (String) receivedData.getOrDefault("eventId", "");
        System.out.println("[TicketTaskService] Checking ticket availability for event: " + eventId);

        try {
            Map<String, String> filters = new HashMap<>();
            filters.put("eventId", eventId);

            Ticket ticket = ticketFetcher.fetch(filters);

            if (ticket == null) {
                throw new Exception("Could not fetch ticket info for event: " + eventId);
            }

            if (!ticket.available) {
                throw new Exception("Tickets not available yet for event: " + eventId);
            }

            System.out.println("[TicketTaskService] Tickets available! Price: " + ticket.price);
            returnData.put("ticketsAvailable", true);
            returnData.put("ticketUrl",   ticket.url);
            returnData.put("ticketPrice", ticket.price);

        } catch (Exception e) {
            System.out.println("[TicketTaskService] ERROR: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}