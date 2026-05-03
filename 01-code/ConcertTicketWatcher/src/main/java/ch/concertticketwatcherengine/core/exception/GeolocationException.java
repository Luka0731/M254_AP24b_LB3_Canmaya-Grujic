package ch.concertticketwatcherengine.core.exception;

/**
 * Thrown when the geolocation API fails to determine the user's location.
 */
public class GeolocationException extends Exception {
    public GeolocationException(String message) {
        super("Geolocation failed: " + message);
    }
}