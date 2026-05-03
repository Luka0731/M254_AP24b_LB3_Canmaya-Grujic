package ch.concertticketwatcherengine.core.generic;

import ch.concertticketwatcherengine.core.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public abstract class Fetcher<M extends Model> {

    public final M fetch(Map<String, String> filters) throws Exception {
        HttpURLConnection api = (HttpURLConnection) new URL(buildUrl(filters)).openConnection();
        api.setRequestMethod("GET");
        api.setRequestProperty("Accept", "application/json");

        BufferedReader reader = new BufferedReader(new InputStreamReader(api.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) response.append(line);
        reader.close();

        M result = mapResponseJsonToModel(response);
        Log.success("{" + getClass().getSimpleName() + "} API fetch successful");
        return result;
    }

    /**
     * Build the full API URL using the provided filters.
     * Filters are passed in from the service and will change per call.
     *
     * @param filters key-value pairs for filtering the API request e.g. "artist", "city"
     * @return the full URL string to call
     */
    protected abstract String buildUrl(Map<String, String> filters);

    /**
     * Parse the raw JSON response and extract only the fields you need.
     * Return them as a flat Map to be passed back to the service.
     *
     * @param response the raw JSON response from the API
     * @return a Map containing only the data your service needs
     */
    protected abstract M mapResponseJsonToModel(StringBuilder response) throws Exception;
}