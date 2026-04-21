package ru.netology.model;

import jakarta.validation.constraints.NotBlank;

public class ConfirmRequest {

    @NotBlank
    private String operationId;

    @NotBlank
    private String code;

    public String getOperationId() { return operationId; }
    public void setOperationId(String operationId) { this.operationId = operationId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
