package ch.concertticketwatcherengine.core.exception;

/**
 * Thrown when a database operation fails unexpectedly.
 */
public class DatabaseException extends Exception {
    public DatabaseException(String operation, String cause) {
        super("Database error during [" + operation + "]: " + cause);
    }
}