package ch.concertticketwatcherengine.core.generic;

import ch.concertticketwatcherengine.core.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public abstract class Fetcher<M extends Model> {

    public final M fetch(Map<String, String> filters) throws Exception {
        String url = buildUrl(filters);
        Log.debug("{" + getClass().getSimpleName() + "} Fetching URL: " + url);

        HttpURLConnection api = (HttpURLConnection) new URL(url).openConnection();
        api.setRequestMethod("GET");
        api.setRequestProperty("Accept", "application/json");

        int status = api.getResponseCode();
        Log.debug("{" + getClass().getSimpleName() + "} Response status: " + status);

        BufferedReader reader = new BufferedReader(new InputStreamReader(api.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) response.append(line);
        reader.close();

        Log.debug("{" + getClass().getSimpleName() + "} Raw response: " + response);

        M result = mapResponseJsonToModel(response);
        Log.debug("{" + getClass().getSimpleName() + "} Mapped result: " + (result == null ? "null (nothing found)" : result.getClass().getSimpleName()));
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
     * Return null if no relevant data was found in the response.
     *
     * @param response the raw JSON response from the API
     * @return the mapped model, or null if nothing was found
     */
    protected abstract M mapResponseJsonToModel(StringBuilder response) throws Exception;
}