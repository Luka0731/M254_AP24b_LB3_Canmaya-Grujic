package ch.concertticketwatcherengine.component.messenger;

import ch.concertticketwatcherengine.core.exception.MessagingException;
import ch.concertticketwatcherengine.core.generic.Messenger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InviteeStartMessenger extends Messenger {

    @Override
    protected List<String> variablesNeeded() {
        return List.of("invitee", "userName", "eventName", "eventVenue", "eventCity", "eventDate", "eventPrice", "eventUrl", "eventImageUrl");
    }

    @Override
    protected void send(String sourceProcessInstanceId, Map<String, Object> variables) throws MessagingException {
        Map<String, Object> vars = new HashMap<>(variables);
        vars.put("subProcessInstanceId", sourceProcessInstanceId);
        startByMessage("InviteeStart", vars);
    }
}