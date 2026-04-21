package ru.netology.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import ru.netology.model.ConfirmRequest;
import ru.netology.model.SuccessResponse;
import ru.netology.model.TransferRequest;
import ru.netology.service.TransferService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransferControllerTest {

    private final TransferService transferService = mock(TransferService.class);
    private final TransferController controller = new TransferController(transferService);

    @Test
    void testTransferSuccess() {
        when(transferService.initiateTransfer(any()))
                .thenReturn("test-operation-id");

        // 10000 копеек = 100.00 рублей
        TransferRequest request = new TransferRequest(
                "1111222233334444",
                "12/25",
                "123",
                "5555666677778888",
                new TransferRequest.Amount(10000, "RUB")
        );

        ResponseEntity<SuccessResponse> response = controller.transfer(request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("test-operation-id", response.getBody().getOperationId());
    }

    @Test
    void testConfirmOperationSuccess() {
        when(transferService.confirmOperation("test-op-id", "0000"))
                .thenReturn("test-op-id");

        ConfirmRequest confirmRequest = new ConfirmRequest();
        confirmRequest.setOperationId("test-op-id");
        confirmRequest.setCode("0000");

        ResponseEntity<SuccessResponse> response = controller.confirmOperation(confirmRequest);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("test-op-id", response.getBody().getOperationId());
    }
}