package ch.concertticketwatcherengine.component.fetcher;

import ch.concertticketwatcherengine.component.model.Location;
import ch.concertticketwatcherengine.core.exception.GeolocationException;
import ch.concertticketwatcherengine.core.generic.Fetcher;
import org.json.JSONObject;
import java.util.Map;

public class GeolocationFetcher extends Fetcher<Location> {

    @Override
    protected String buildUrl(Map<String, String> filters) {
        String ip = filters.getOrDefault("ip", "");
        return "http://ip-api.com/json/" + ip + "?fields=lat,lon,city,status,message";
    }

    @Override
    protected Location mapResponseJsonToModel(StringBuilder response) throws GeolocationException {
        JSONObject json = new JSONObject(response.toString());

        if (!"success".equals(json.optString("status"))) {
            throw new GeolocationException(json.optString("message", "unknown error"));
        }

        Location location = new Location();
        location.setLatitude(json.getDouble("lat"));
        location.setLongitude(json.getDouble("lon"));
        location.setCity(json.getString("city"));
        return location;
    }
}