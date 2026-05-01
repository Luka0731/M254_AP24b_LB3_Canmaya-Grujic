package ch.concertticketwatcherengine.component.fetcher;

import ch.concertticketwatcherengine.component.model.Location;
import ch.concertticketwatcherengine.core.generic.Fetcher;
import org.json.JSONObject;

import java.util.Map;

public class GeolocationFetcher extends Fetcher<Location> {

    // If no IP is passed, it auto-detects the current machine's IP
    @Override
    protected String buildUrl(Map<String, String> filters) {
        String ip = filters.getOrDefault("ip", "");
        return "http://ip-api.com/json/" + ip + "?fields=lat,lon,city,status,message";
    }

    @Override
    protected Location mapResponseJsonToModel(StringBuilder response) throws Exception {
        JSONObject json = new JSONObject(response.toString());

        if (!"success".equals(json.optString("status"))) {
            throw new Exception("Geolocation failed: " + json.optString("message", "unknown error"));
        }

        Location location = new Location();
        location.latitude  = json.getDouble("lat");
        location.longitude = json.getDouble("lon");
        location.city      = json.getString("city");
        return location;
    }
}