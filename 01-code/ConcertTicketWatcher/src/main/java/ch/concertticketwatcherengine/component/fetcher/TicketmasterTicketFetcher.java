package ch.concertticketwatcherengine.component.fetcher;

import ch.concertticketwatcherengine.component.model.Ticket;
import ch.concertticketwatcherengine.core.generic.Fetcher;
import java.util.Map;

public class TicketmasterTicketFetcher extends Fetcher<Ticket> {

    @Override
    protected String buildUrl(Map<String, String> filters) {
        // TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        return "";
    }

    @Override
    protected Ticket mapResponseJsonToModel(StringBuilder response) throws Exception {
        // TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        return null;
    }
}