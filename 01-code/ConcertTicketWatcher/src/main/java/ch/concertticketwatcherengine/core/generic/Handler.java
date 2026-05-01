package ch.concertticketwatcherengine.core.generic;

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
        // |----- setup data -----|
        receivedData = new HashMap<>();
        for (String key : defineReceivedData()) {
            receivedData.put(key, externalTask.getVariable(key));
        }
        returnData = new HashMap<>();
        for (String key : defineReturnData()) {
            returnData.put(key, null);
        }
        service.setData(receivedData, returnData, externalTaskService);

        // |----- run service -----|
        try {
            service.execute();
        }  catch (Exception e) {
            System.out.println("\u001B[31mTask [" + externalTask.getTopicName() + "] failed: " + e.getMessage() + "\u001B[0m");
            externalTaskService.handleFailure(externalTask, e.getMessage(), null, 0, 0L);
            return;
        }

        // |----- end task & return data -----|
        if (returnData == null) {
            externalTaskService.complete(externalTask);
        }  else {
            returnData = service.getReturnData();
            externalTaskService.complete(externalTask, returnData);
        }
    }

    /**
     * Define the variables this handler expects to RECEIVE from the Camunda process.
     * The keys must match the variable names defined in your BPMN diagram exactly.
     *
     * @return a List with the expected Camunda variable names as keys, values can be null
     */
    protected abstract List<String> defineReceivedData();

    /**
     * Define the variables this handler will SEND BACK to the Camunda process on completion.
     * The keys must match the variable names defined in your BPMN diagram exactly.
     *
     * @return a Map with the Camunda variable names as keys, values can be null
     */
    protected abstract List<String> defineReturnData();
}