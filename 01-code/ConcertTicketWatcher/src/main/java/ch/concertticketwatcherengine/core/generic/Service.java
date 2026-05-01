package ch.concertticketwatcherengine.core.generic;

import org.camunda.bpm.client.task.ExternalTaskService;

import java.util.Map;

public abstract class Service {

    protected Map<String, Object> receivedData;
    protected Map<String, Object> returnData;
    protected ExternalTaskService externalTaskService;

    public void setData(Map<String, Object> receivedData, Map<String, Object> returnData, ExternalTaskService externalTaskService) {
        this.receivedData = receivedData;
        this.returnData = returnData;
        this.externalTaskService = externalTaskService;
    }

    public Map<String, Object> getReturnData() {
        return returnData;
    }

    /**
     * Contains the main business logic for this Camunda task.
     * Use receivedData to read variables passed in from the handler.
     * Use returnData to store results that will be sent back to Camunda.
     * Use externalTaskService to report failures back to Camunda if needed and throw an exception.
     */
    public abstract void execute();
}