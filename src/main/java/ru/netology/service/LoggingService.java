package ru.netology.service;


import ru.netology.dto.TransferRequest;

public interface LoggingService {
    void logTransfer(TransferRequest request, String operationId, String status, double commission);
}