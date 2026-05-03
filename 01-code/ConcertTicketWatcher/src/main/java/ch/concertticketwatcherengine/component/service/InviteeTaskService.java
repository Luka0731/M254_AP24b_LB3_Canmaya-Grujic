package ch.concertticketwatcherengine.component.service;

import ch.concertticketwatcherengine.component.fetcher.CamundaUserFetcher;
import ch.concertticketwatcherengine.component.model.CamundaUser;
import ch.concertticketwatcherengine.core.generic.Service;
import ch.concertticketwatcherengine.core.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InviteeTaskService extends Service {

    private final CamundaUserFetcher camundaUserFetcher;

    public InviteeTaskService(CamundaUserFetcher camundaUserFetcher) {
        this.camundaUserFetcher = camundaUserFetcher;
    }

    @Override
    public void execute() {
        String raw = (String) receivedData.getOrDefault("inviteeUsernames", "");
        List<Map<String, String>> invitees = resolveInvitees(raw);
        returnData.put("inviteeList", invitees);
    }



    // |----- helper methods -----|

    private List<Map<String, String>> resolveInvitees(String raw) {
        List<Map<String, String>> result = new ArrayList<>();
        if (raw == null || raw.isBlank()) return result;

        for (String part : raw.split(",")) {
            String username = part.strip();
            if (!username.isEmpty()) processUsername(username, result);
        }
        return result;
    }

    private void processUsername(String username, List<Map<String, String>> result) {
        try {
            Map<String, String> filters = new HashMap<>();
            filters.put("username", username);

            CamundaUser user = camundaUserFetcher.fetch(filters);
            if (user != null) {
                Map<String, String> invitee = new HashMap<>();
                invitee.put("username", user.getUsername());
                invitee.put("email",    user.getEmail());
                result.add(invitee);
            } else {
                Log.error("{InviteeTaskService} Skipping unknown user or no email set: " + username);
            }
        } catch (Exception e) {
            Log.error("{InviteeTaskService} Could not resolve '" + username + "': " + e.getMessage());
        }
    }
}