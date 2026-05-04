package ch.concertticketwatcherengine.core.exception;

/**
 * Thrown when sending a message to another Camunda process fails.
 */
public class MessagingException extends Exception {
    public MessagingException(String messageName, String cause) {
        super("Failed to send message [" + messageName + "]: " + cause);
    }
}