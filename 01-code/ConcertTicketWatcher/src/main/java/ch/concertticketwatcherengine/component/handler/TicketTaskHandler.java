package ch.concertticketwatcherengine.component.handler;

import ch.concertticketwatcherengine.component.service.TicketTaskService;
import ch.concertticketwatcherengine.core.generic.Handler;
import java.util.List;

public class TicketTaskHandler extends Handler {

    public TicketTaskHandler(TicketTaskService service) {
        super(service);
    }

    @Override
    protected List<String> defineReceivedData() {
        return List.of("eventId", "eventName", "eventVenue", "eventCity", "eventDate", "eventUrl", "email", "userName", "isGoing", "inviteeList");
    }

    @Override
    protected List<String> defineReturnData() {
        return List.of("ticketsAvailable", "ticketUrl", "ticketPrice");
    }
}