package ch.concertticketwatcherengine.component.fetcher;

import ch.concertticketwatcherengine.component.model.Location;
import ch.concertticketwatcherengine.core.generic.Fetcher;
import java.util.Map;

public class GeolocationFetcher extends Fetcher<Location> {

    @Override
    protected String buildUrl(Map<String, String> filters) {
        // TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        return "";
    }

    @Override
    protected Location mapResponseJsonToModel(StringBuilder response) throws Exception {
        // TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        return null;
    }
}