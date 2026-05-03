package ch.concertticketwatcherengine.core.exception;

/**
 * Thrown when the Ticketmaster API returns an unexpected response or is unreachable.
 */
public class TicketmasterApiException extends Exception {
    public TicketmasterApiException(String message) {
        super("Ticketmaster API error: " + message);
    }
}