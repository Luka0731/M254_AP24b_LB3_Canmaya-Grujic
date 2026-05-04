package ch.concertticketwatcherengine.component.messenger;

import ch.concertticketwatcherengine.core.exception.MessagingException;
import ch.concertticketwatcherengine.core.generic.Messenger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvitationResultMessenger extends Messenger {

    @Override
    protected List<String> variablesNeeded() {
        return List.of("userProcessInstanceId", "inviteesAccepted", "inviteesDeclined");
    }

    @Override
    protected void send(String sourceProcessInstanceId, Map<String, Object> variables) throws MessagingException {
        String target = (String) variables.get("userProcessInstanceId");
        Map<String, Object> vars = new HashMap<>();
        vars.put("inviteesAccepted", variables.get("inviteesAccepted"));
        vars.put("inviteesDeclined", variables.get("inviteesDeclined"));
        correlate("InvitationResult", target, vars);
    }
}