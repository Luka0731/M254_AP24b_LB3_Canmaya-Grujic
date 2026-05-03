package ch.concertticketwatcherengine.core.exception;

/**
 * Thrown when tickets are not yet on sale for the event.
 * This is an expected outcome. The system will retry on the next poll cycle.
 */
public class TicketsNotAvailableException extends Exception {
    public TicketsNotAvailableException(String eventId) {
        super("Tickets not available yet for event: " + eventId);
    }
}