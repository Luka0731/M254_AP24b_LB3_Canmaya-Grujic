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
        return null;
    }

    @Override
    protected List<String> defineReturnData() {
        return null;
    }
}