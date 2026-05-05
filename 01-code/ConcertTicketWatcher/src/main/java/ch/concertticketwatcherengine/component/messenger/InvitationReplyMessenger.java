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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvitationReplyMessenger extends Messenger {

    private static final String CAMUNDA_BASE = "http://localhost:8080/engine-rest";

    @Override
    protected List<String> variablesNeeded() {
        return List.of("subProcessInstanceId", "inviteeUsername", "isGoing");
    }

    @Override
    protected void send(String sourceProcessInstanceId, Map<String, Object> variables) throws MessagingException {
        String parentProcessInstanceId = (String) variables.get("subProcessInstanceId");
        String inviteeUsername = (String) variables.get("inviteeUsername");

        String executionId = findExecutionId(parentProcessInstanceId);
        Log.debug("{InvitationReplyMessenger} Triggering execution: " + executionId + " for invitee: " + inviteeUsername);

        Map<String, Object> vars = new HashMap<>();
        vars.put("inviteeUsername", inviteeUsername);
        vars.put("isGoing", variables.get("isGoing"));

        triggerExecution(executionId, vars);
    }

    private String findExecutionId(String processInstanceId) throws MessagingException {
        try {
            String url = CAMUNDA_BASE + "/execution?processInstanceId=" + processInstanceId
                + "&messageEventSubscriptionName=InvitationReply";
            Log.debug("{InvitationReplyMessenger} Looking up execution: " + url);

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();

            Log.debug("{InvitationReplyMessenger} Executions found: " + response);
            JSONArray executions = new JSONArray(response.toString());

            if (executions.isEmpty()) throw new MessagingException("InvitationReply", "No waiting execution found in process: " + processInstanceId);

            return executions.getJSONObject(0).getString("id");

        } catch (MessagingException e) {
            throw e;
        } catch (Exception e) {
            throw new MessagingException("InvitationReply", "Failed to find execution: " + e.getMessage());
        }
    }

    private void triggerExecution(String executionId, Map<String, Object> vars) throws MessagingException {
        try {
            JSONObject body = new JSONObject();
            JSONObject jsonVars = new JSONObject();
            for (Map.Entry<String, Object> entry : vars.entrySet()) {
                if (entry.getValue() == null) continue;
                jsonVars.put(entry.getKey(), new JSONObject()
                    .put("value", entry.getValue())
                    .put("type", entry.getValue() instanceof Boolean ? "Boolean" : "String"));
            }
            body.put("variables", jsonVars);

            String url = CAMUNDA_BASE + "/execution/" + executionId + "/messageSubscriptions/InvitationReply/trigger";
            Log.debug("{InvitationReplyMessenger} Triggering: " + url);

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setDoOutput(true);
            conn.getOutputStream().write(body.toString().getBytes(StandardCharsets.UTF_8));

            int status = conn.getResponseCode();
            Log.debug("{InvitationReplyMessenger} Trigger status: " + status);
            if (status < 200 || status >= 300) throw new MessagingException("InvitationReply", "Trigger returned HTTP " + status);

        } catch (MessagingException e) {
            throw e;
        } catch (Exception e) {
            throw new MessagingException("InvitationReply", "Failed to trigger execution: " + e.getMessage());
        }
    }
}