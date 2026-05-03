package ch.concertticketwatcherengine.core.exception;

/**
 * Thrown when a required configuration value is missing or invalid.
 * Usually means config.properties is missing or incomplete.
 */
public class ConfigurationException extends RuntimeException {
    public ConfigurationException(String key) {
        super("Missing or invalid configuration key: " + key + ". Check your config.properties file.");
    }
}