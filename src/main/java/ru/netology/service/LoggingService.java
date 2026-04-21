package ru.netology.service;


import ru.netology.model.TransferRequest;

public interface LoggingService {
    void logTransfer(TransferRequest request, String operationId, String status, double commission);
}