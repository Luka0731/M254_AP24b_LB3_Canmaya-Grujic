package ch.concertticketwatcherengine.core.setup;

import ch.concertticketwatcherengine.core.exception.ConfigurationException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads configuration values from config.properties in the resources folder.
 */
public class Config {

    private static final Properties properties = new Properties();

    static {
        try (InputStream inputStream = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (inputStream == null) {
                throw new ConfigurationException("config.properties not found! Copy config.example.properties and fill in your values.");
            }
            properties.load(inputStream);
        } catch (ConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigurationException("Failed to load config.properties: " + e.getMessage());
        }
    }

    public static String get(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new ConfigurationException(key);
        }
        return value;
    }
}
