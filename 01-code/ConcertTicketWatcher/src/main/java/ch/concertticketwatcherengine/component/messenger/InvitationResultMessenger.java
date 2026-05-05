package ch.concertticketwatcherengine.component.messenger;

import ch.concertticketwatcherengine.core.exception.MessagingException;
import ch.concertticketwatcherengine.core.generic.Messenger;
import ch.concertticketwatcherengine.core.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvitationResultMessenger extends Messenger {

    /**
     * DISCLAIMER!
     * Messenger Classes are supposed to have no logic in them. Mainly sending data from one process to the other.
     * This here is an exception. It is a cheep work around, to not use another service task. (Technical Depth)
     */

    private static final String CAMUNDA_BASE = "http://localhost:8080/engine-rest";

    @Override
    protected List<String> variablesNeeded() {
        return List.of("userProcessInstanceId", "inviteeList");
    }

    @Override
    protected void send(String sourceProcessInstanceId, Map<String, Object> variables) throws MessagingException {
        String target = (String) variables.get("userProcessInstanceId");
        Object raw = variables.get("inviteeList");

        List<String> accepted = new ArrayList<>();
        List<String> declined = new ArrayList<>();

        if (raw instanceof List) {
            for (Object item : (List<?>) raw) {
                if (!(item instanceof Map)) continue;
                Map<?, ?> invitee = (Map<?, ?>) item;
                String username = (String) invitee.get("username");
                if (username == null) continue;

                boolean isGoing = fetchInviteeIsGoing(sourceProcessInstanceId, username);
                Log.debug("{InvitationResultMessenger} " + username + " isGoing: " + isGoing);
                if (isGoing) accepted.add(username);
                else declined.add(username);
            }
        }

        Log.debug("{InvitationResultMessenger} Accepted: " + accepted + " | Declined: " + declined);

        Map<String, Object> vars = new HashMap<>();
        vars.put("inviteesAccepted", String.join(", ", accepted));
        vars.put("inviteesDeclined", String.join(", ", declined));
        correlate("InvitationResult", target, vars);
    }

    private boolean fetchInviteeIsGoing(String parentProcessInstanceId, String inviteeUsername) {
        try {
            String url = CAMUNDA_BASE + "/history/variable-instance"
                + "?variableName=isGoing"
                + "&processInstanceIdIn=" + findInviteeProcessInstanceId(parentProcessInstanceId, inviteeUsername);
            Log.debug("{InvitationResultMessenger} Fetching isGoing for " + inviteeUsername + ": " + url);

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();

            JSONArray vars = new JSONArray(response.toString());
            if (vars.isEmpty()) return false;
            return Boolean.TRUE.equals(vars.getJSONObject(0).optBoolean("value", false));

        } catch (Exception e) {
            Log.error("{InvitationResultMessenger} Could not fetch isGoing for " + inviteeUsername + ": " + e.getMessage());
            return false;
        }
    }

    private String findInviteeProcessInstanceId(String parentProcessInstanceId, String inviteeUsername) throws Exception {
        String url = CAMUNDA_BASE + "/history/variable-instance"
            + "?variableName=inviteeUsername"
            + "&variableValue=" + inviteeUsername
            + "&variableValueEquals=true";

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) response.append(line);
        reader.close();

        Log.debug("{InvitationResultMessenger} Process lookup for " + inviteeUsername + ": " + response);
        JSONArray results = new JSONArray(response.toString());

        // find the one that belongs to an invitee process (not the sub-process parent)
        for (int i = 0; i < results.length(); i++) {
            JSONObject entry = results.getJSONObject(i);
            String processInstanceId = entry.getString("processInstanceId");
            if (!processInstanceId.equals(parentProcessInstanceId)) {
                return processInstanceId;
            }
        }
        throw new Exception("No invitee process found for username: " + inviteeUsername);
    }
}