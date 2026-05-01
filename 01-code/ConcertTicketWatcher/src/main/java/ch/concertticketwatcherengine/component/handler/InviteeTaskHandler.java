package ch.concertticketwatcherengine.component.handler;

import ch.concertticketwatcherengine.component.service.InviteeTaskService;
import ch.concertticketwatcherengine.core.generic.Handler;
import java.util.List;

public class InviteeTaskHandler extends Handler {

    public InviteeTaskHandler(InviteeTaskService service) {
        super(service);
    }

    @Override
    protected List<String> defineReceivedData() {
        return List.of("inviteeUsernames");
    }

    @Override
    protected List<String> defineReturnData() {
        return List.of("inviteeList");
    }
}