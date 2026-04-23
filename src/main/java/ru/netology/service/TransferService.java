package ru.netology.service;

import ru.netology.dto.TransferRequest;

public interface TransferService {
    String initiateTransfer(TransferRequest request);
    String confirmOperation(String operationId, String code);
}
