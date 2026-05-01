package ch.concertticketwatcherengine.component.handler;

import ch.concertticketwatcherengine.component.service.ConcertTaskService;
import ch.concertticketwatcherengine.core.generic.Handler;
import java.util.List;

public class ConcertTaskHandler extends Handler {

    public ConcertTaskHandler(ConcertTaskService service) {
        super(service);
    }

    @Override
    protected List<String> defineReceivedData() {
        return List.of("artistName", "maxDistanceKm", "userName", "email");
    }

    @Override
    protected List<String> defineReturnData() {
        return List.of("eventName", "eventVenue", "eventCity", "eventDate", "eventPrice", "eventUrl", "eventImageUrl");
    }
}