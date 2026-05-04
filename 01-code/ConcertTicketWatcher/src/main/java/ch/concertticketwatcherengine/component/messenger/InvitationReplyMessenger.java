package ch.concertticketwatcherengine.component.messenger;

import ch.concertticketwatcherengine.core.exception.MessagingException;
import ch.concertticketwatcherengine.core.generic.Messenger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvitationReplyMessenger extends Messenger {

    @Override
    protected List<String> variablesNeeded() {
        return List.of("subProcessInstanceId", "invitee", "isGoing");
    }

    @Override
    protected void send(String sourceProcessInstanceId, Map<String, Object> variables) throws MessagingException {
        String target = (String) variables.get("subProcessInstanceId");
        Map<String, Object> vars = new HashMap<>();
        vars.put("invitee", variables.get("invitee"));
        vars.put("isGoing", variables.get("isGoing"));
        correlate("InvitationReply", target, vars);
    }
}