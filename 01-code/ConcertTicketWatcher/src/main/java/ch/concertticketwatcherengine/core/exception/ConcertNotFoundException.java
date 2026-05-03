package ch.concertticketwatcherengine.core.exception;

/**
 * Thrown when no concert is found for the watched artist.
 * This is an expected outcome. The system will retry on the next poll cycle.
 */
public class ConcertNotFoundException extends Exception {
    public ConcertNotFoundException(String artistName) {
        super("No concert found for artist: " + artistName);
    }
}