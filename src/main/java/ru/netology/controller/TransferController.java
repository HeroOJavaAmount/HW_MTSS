package ru.netology.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.netology.dto.ConfirmRequest;
import ru.netology.dto.SuccessResponse;
import ru.netology.dto.TransferRequest;
import ru.netology.service.TransferService;

@RestController
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<SuccessResponse> transfer(@Valid @RequestBody TransferRequest request) {
        String operationId = transferService.initiateTransfer(request);
        return ResponseEntity.ok(new SuccessResponse(operationId));
    }

    @PostMapping("/confirmOperation")
    public ResponseEntity<SuccessResponse> confirmOperation(@Valid @RequestBody ConfirmRequest request) {
        String operationId = transferService.confirmOperation(
                request.getOperationId(),
                request.getCode()
        );
        return ResponseEntity.ok(new SuccessResponse(operationId));
    }
}