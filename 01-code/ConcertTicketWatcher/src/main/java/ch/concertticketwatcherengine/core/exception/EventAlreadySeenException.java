package ch.concertticketwatcherengine.core.exception;

/**
 * Thrown when the user has already been notified about this event.
 * This is an expected outcome. The system will retry on the next poll cycle.
 */
public class EventAlreadySeenException extends Exception {
    public EventAlreadySeenException(String eventId) {
        super("Event already announced: " + eventId);
    }
}