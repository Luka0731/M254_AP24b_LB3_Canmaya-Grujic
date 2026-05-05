package ch.concertticketwatcherengine.component.messenger;

import ch.concertticketwatcherengine.component.fetcher.CamundaUserFetcher;
import ch.concertticketwatcherengine.component.model.CamundaUser;
import ch.concertticketwatcherengine.core.exception.MessagingException;
import ch.concertticketwatcherengine.core.generic.Messenger;
import ch.concertticketwatcherengine.core.util.Log;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemStartMessenger extends Messenger {

    private final CamundaUserFetcher camundaUserFetcher;

    public SystemStartMessenger(CamundaUserFetcher camundaUserFetcher) {
        this.camundaUserFetcher = camundaUserFetcher;
    }

    @Override
    protected List<String> variablesNeeded() {
        return List.of("artistName", "maxDistanceKm", "initiator");
    }

    @Override
    protected void send(String sourceProcessInstanceId, Map<String, Object> variables) throws MessagingException {
        String initiator = (String) variables.get("initiator");
        Map<String, Object> vars = new HashMap<>(variables);
        vars.put("userProcessInstanceId", sourceProcessInstanceId);
        vars.remove("initiator");

        try {
            Map<String, String> filters = new HashMap<>();
            filters.put("username", initiator);
            CamundaUser user = camundaUserFetcher.fetch(filters);
            if (user == null) throw new MessagingException("SystemStart", "User not found or has no email set in Camunda: " + initiator);
            vars.put("userName", user.getUsername());
            vars.put("email", user.getEmail());
            Log.debug("{SystemStartMessenger} Fetched user: " + user.getUsername() + " | " + user.getEmail());
        } catch (MessagingException e) {
            throw e;
        } catch (Exception e) {
            throw new MessagingException("SystemStart", "Failed to fetch user profile: " + e.getMessage());
        }

        startByMessage("SystemStart", vars);
    }
}