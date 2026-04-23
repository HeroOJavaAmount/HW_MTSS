package ru.netology.dto;

public class SuccessResponse {
    private String operationId;

    public SuccessResponse() {}

    public SuccessResponse(String operationId) {
        this.operationId = operationId;
    }

    public String getOperationId() { return operationId; }
    public void setOperationId(String operationId) { this.operationId = operationId; }
}
