package ch.concertticketwatcherengine.core.generic;

import ch.concertticketwatcherengine.core.exception.MessagingException;
import ch.concertticketwatcherengine.core.util.Log;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Messenger implements ExternalTaskHandler {

    private static final String CAMUNDA_URL = "http://localhost:8080/engine-rest/message";

    @Override
    public final void execute(ExternalTask task, ExternalTaskService taskService) {
        Log.info("{" + task.getTopicName() + "} Messenger started (process: " + task.getProcessInstanceId() + ")");
        try {
            Map<String, Object> variables = collectVariables(task);
            Log.debug("{" + task.getTopicName() + "} Collected variables: " + variables);
            send(task.getProcessInstanceId(), variables);
            taskService.complete(task);
            Log.success("{" + task.getTopicName() + "} Message sent");
        } catch (MessagingException e) {
            Log.error("{" + task.getTopicName() + "} " + e.getMessage());
            taskService.handleBpmnError(task, "TASK_FAILED", e.getMessage());
        }
    }

    /**
     * Define which process variables this messenger needs to read and forward.
     * Keys must match variable names in the BPMN exactly.
     *
     * @return list of variable names to collect from the current process instance
     */
    protected abstract List<String> variablesNeeded();

    /**
     * Perform the actual message delivery using {@link #startByMessage} or {@link #correlate}.
     *
     * @param sourceProcessInstanceId the process instance this messenger is running in
     * @param variables variables collected from {@link #variablesNeeded()}
     */
    protected abstract void send(String sourceProcessInstanceId, Map<String, Object> variables) throws MessagingException;



    // |----- helper methods -----|

    /**
     * Starts a new process instance via a message start event.
     */
    protected void startByMessage(String messageName, Map<String, Object> vars) throws MessagingException {
        Log.debug("{Messenger} startByMessage: " + messageName + " vars: " + vars);
        post(messageName, buildBody(messageName, null, null, null, vars));
    }

    /**
     * Starts a new process instance and also sets a local variable on the triggered execution.
     * Use this to set local scope variables that can be used for correlation later.
     */
    protected void startByMessageWithLocalVar(String messageName, String localVarName, String localVarValue, Map<String, Object> vars) throws MessagingException {
        Log.debug("{Messenger} startByMessageWithLocalVar: " + messageName + " localVar: " + localVarName + "=" + localVarValue);
        post(messageName, buildBodyWithLocalVars(messageName, null, localVarName, localVarValue, vars));
    }

    /**
     * Correlates a message to an already running process instance.
     */
    protected void correlate(String messageName, String targetProcessInstanceId, Map<String, Object> vars) throws MessagingException {
        Log.debug("{Messenger} correlate: " + messageName + " -> process: " + targetProcessInstanceId + " vars: " + vars);
        post(messageName, buildBody(messageName, targetProcessInstanceId, null, null, vars));
    }

    /**
     * Correlates a message to a specific execution using a local variable as a correlation key.
     * Use when multiple executions in the same process instance wait on the same message name.
     */
    protected void correlateWithLocalKey(String messageName, String targetProcessInstanceId, String localKeyName, String localKeyValue, Map<String, Object> vars) throws MessagingException {
        Log.debug("{Messenger} correlateWithLocalKey: " + messageName + " -> process: " + targetProcessInstanceId + " localKey: " + localKeyName + "=" + localKeyValue);
        post(messageName, buildBody(messageName, targetProcessInstanceId, localKeyName, localKeyValue, vars));
    }

    private Map<String, Object> collectVariables(ExternalTask task) {
        Map<String, Object> vars = new HashMap<>();
        for (String key : variablesNeeded()) vars.put(key, task.getVariable(key));
        return vars;
    }

    private String buildBody(String messageName, String processInstanceId, String localKeyName, String localKeyValue, Map<String, Object> vars) {
        JSONObject body = new JSONObject();
        body.put("messageName", messageName);
        if (processInstanceId != null) body.put("processInstanceId", processInstanceId);
        if (localKeyName != null) {
            JSONObject localVars = new JSONObject();
            localVars.put(localKeyName, new JSONObject().put("value", localKeyValue).put("type", "String"));
            body.put("localCorrelationKeys", localVars);
        }
        JSONObject jsonVars = new JSONObject();
        for (Map.Entry<String, Object> entry : vars.entrySet()) {
            if (entry.getValue() == null) continue;
            jsonVars.put(entry.getKey(), new JSONObject()
                .put("value", entry.getValue())
                .put("type", resolveType(entry.getValue())));
        }
        body.put("processVariables", jsonVars);
        Log.debug("{Messenger} Built request body: " + body);
        return body.toString();
    }

    private String buildBodyWithLocalVars(String messageName, String processInstanceId, String localVarName, String localVarValue, Map<String, Object> vars) {
        JSONObject body = new JSONObject();
        body.put("messageName", messageName);
        if (processInstanceId != null) body.put("processInstanceId", processInstanceId);
        JSONObject jsonVars = new JSONObject();
        for (Map.Entry<String, Object> entry : vars.entrySet()) {
            if (entry.getValue() == null) continue;
            jsonVars.put(entry.getKey(), new JSONObject()
                .put("value", entry.getValue())
                .put("type", resolveType(entry.getValue())));
        }
        body.put("processVariables", jsonVars);
        if (localVarName != null) {
            JSONObject localVars = new JSONObject();
            localVars.put(localVarName, new JSONObject().put("value", localVarValue).put("type", "String"));
            body.put("processVariablesLocal", localVars);
        }
        Log.debug("{Messenger} Built request body with local vars: " + body);
        return body.toString();
    }

    private String resolveType(Object value) {
        if (value instanceof Boolean) return "Boolean";
        if (value instanceof Integer) return "Integer";
        if (value instanceof Long)    return "Long";
        if (value instanceof Double)  return "Double";
        return "String";
    }

    private void post(String messageName, String body) throws MessagingException {
        try {
            Log.debug("{Messenger} POSTing to: " + CAMUNDA_URL);
            HttpURLConnection connection = (HttpURLConnection) new URL(CAMUNDA_URL).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Accept-Charset", "utf-8");
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
            int status = connection.getResponseCode();
            Log.debug("{Messenger} Response status: " + status);
            if (status < 200 || status >= 300) {
                BufferedReader errReader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
                StringBuilder errBody = new StringBuilder();
                String line;
                while ((line = errReader.readLine()) != null) errBody.append(line);
                errReader.close();
                Log.debug("{Messenger} Error response body: " + errBody);
                throw new MessagingException(messageName, "Camunda REST returned HTTP " + status + " — " + errBody);
            }
        } catch (MessagingException e) {
            throw e;
        } catch (Exception e) {
            throw new MessagingException(messageName, e.getMessage());
        }
    }
}