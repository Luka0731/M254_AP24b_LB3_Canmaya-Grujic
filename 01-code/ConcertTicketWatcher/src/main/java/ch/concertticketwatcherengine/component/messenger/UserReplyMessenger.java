package ch.concertticketwatcherengine.component.messenger;

import ch.concertticketwatcherengine.core.exception.MessagingException;
import ch.concertticketwatcherengine.core.generic.Messenger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserReplyMessenger extends Messenger {

    @Override
    protected List<String> variablesNeeded() {
        return List.of("systemProcessInstanceId", "isGoing", "inviteeUsernames");
    }

    @Override
    protected void send(String sourceProcessInstanceId, Map<String, Object> variables) throws MessagingException {
        String target = (String) variables.get("systemProcessInstanceId");
        Map<String, Object> vars = new HashMap<>();
        vars.put("isGoing", variables.get("isGoing"));
        vars.put("inviteeUsernames", variables.get("inviteeUsernames"));
        correlate("UserReply", target, vars);
    }
}