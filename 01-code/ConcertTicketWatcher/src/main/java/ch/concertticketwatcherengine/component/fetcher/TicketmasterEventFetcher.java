package ch.concertticketwatcherengine.component.fetcher;

import ch.concertticketwatcherengine.component.model.Event;
import ch.concertticketwatcherengine.core.generic.Fetcher;
import java.util.Map;

public class TicketmasterEventFetcher extends Fetcher<Event> {

    @Override
    protected String buildUrl(Map<String, String> filters) {
        // TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        return "";
    }

    @Override
    protected Event mapResponseJsonToModel(StringBuilder response) throws Exception {
        // TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        return null;
    }
}