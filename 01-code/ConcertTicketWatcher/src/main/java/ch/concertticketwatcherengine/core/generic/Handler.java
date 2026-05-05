package ch.concertticketwatcherengine.core.generic;

import ch.concertticketwatcherengine.core.util.Log;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Handler implements ExternalTaskHandler {

    private final Service service;
    protected Map<String, Object> receivedData;
    protected Map<String, Object> returnData;

    public Handler(Service service) {
        this.service = service;
    }

    @Override
    public final void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        Log.info("{" + externalTask.getTopicName() + "} Task started (process instance: " + externalTask.getProcessInstanceId() + ")");

        // |----- setup data -----|
        receivedData = new HashMap<>();
        for (String key : defineReceivedData()) {
            receivedData.put(key, externalTask.getVariable(key));
        }
        receivedData.put("processInstanceId", externalTask.getProcessInstanceId());
        Log.debug("{" + externalTask.getTopicName() + "} Received variables: " + receivedData);
        returnData = new HashMap<>();
        for (String key : defineReturnData()) {
            returnData.put(key, null);
        }
        service.setData(receivedData, returnData, externalTaskService);

        // |----- run service -----|
        new Thread(() -> {
            final boolean[] finished = {false};
            Thread keepalive = new Thread(() -> {
                while (!finished[0]) {
                    try {
                        Thread.sleep(5 * 60 * 1000L);
                        if (!finished[0]) externalTaskService.extendLock(externalTask, 10 * 60 * 1000L);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception ignored) {}
                }
            });
            keepalive.setDaemon(true);
            keepalive.start();

            try {
                service.execute();
            } catch (Exception e) {
                Log.error("{" + externalTask.getTopicName() + "} Task failed. This might be intentional: " + e.getMessage());
                finished[0] = true;
                keepalive.interrupt();
                externalTaskService.handleBpmnError(externalTask, "TASK_FAILED", e.getMessage());
                return;
            }

            finished[0] = true;
            keepalive.interrupt();

            // |----- end task & return data -----|
            if (returnData == null) {
                externalTaskService.complete(externalTask);
                Log.success("{" + externalTask.getTopicName() + "} Task completed. No return variables.");
            } else {
                returnData = service.getReturnData();
                Log.debug("{" + externalTask.getTopicName() + "} Returning variables: " + returnData);
                externalTaskService.complete(externalTask, returnData);
            }
        }).start();
    }

    /**
     * Define the variables this handler expects to RECEIVE from the Camunda process.
     * The keys must match the variable names defined in your BPMN diagram exactly.
     * Note: processInstanceId is always injected automatically, no need to list it here.
     *
     * @return a List with the expected Camunda variable names as keys, values can be null
     */
    protected abstract List<String> defineReceivedData();

    /**
     * Define the variables this handler will SEND BACK to the Camunda process on completion.
     * The keys must match the variable names defined in your BPMN diagram exactly.
     *
     * @return a List with the Camunda variable names as keys, values can be null
     */
    protected abstract List<String> defineReturnData();
}