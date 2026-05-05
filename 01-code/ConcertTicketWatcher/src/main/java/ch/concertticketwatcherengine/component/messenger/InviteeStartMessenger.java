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
        Object inviteeRaw = variables.get("invitee");
        String inviteeUsername = extractFromInvitee(inviteeRaw, "username");
        String inviteeEmail    = extractFromInvitee(inviteeRaw, "email");

        Map<String, Object> vars = new HashMap<>(variables);
        vars.remove("invitee");
        vars.put("inviteeUsername",          inviteeUsername);
        vars.put("inviteeEmail",             inviteeEmail);
        vars.put("subProcessInstanceId",     sourceProcessInstanceId);
        startByMessageWithLocalVar("InviteeStart", "inviteeUsername", inviteeUsername, vars);
    }

    private String extractFromInvitee(Object inviteeRaw, String key) {
        if (inviteeRaw instanceof Map) {
            return (String) ((Map<String, Object>) inviteeRaw).getOrDefault(key, "");
        }
        return "";
    }
}