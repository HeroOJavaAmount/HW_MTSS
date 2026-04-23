package ru.netology.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.netology.dto.TransferRequest;
import ru.netology.exception.InvalidInputException;
import ru.netology.exception.TransferException;
import ru.netology.repository.InMemoryTransferRepository;
import ru.netology.service.commission.MixedCommission;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

    @Mock private InMemoryTransferRepository repository;
    @Mock private MixedCommission commission;
    @Mock private LoggingService logger;
    @InjectMocks private TransferServiceImpl service;

    private final TransferRequest validRequest = new TransferRequest(
            "1111222233334444", "12/25", "123", "5555666677778888",
            new TransferRequest.Amount(50000, "RUB")); // 500 руб

    @Test
    void initiateTransfer_ShouldSucceed() {
        when(commission.isValidAmount(new BigDecimal("500.00"))).thenReturn(true);
        when(repository.cardExists("1111222233334444")).thenReturn(true);
        when(repository.cardExists("5555666677778888")).thenReturn(true);
        when(commission.calculate(new BigDecimal("500.00"))).thenReturn(new BigDecimal("5.00"));
        when(repository.hasSufficientFunds(eq("1111222233334444"), eq(new BigDecimal("50500")))).thenReturn(true);
        when(repository.saveOperation(validRequest)).thenReturn("op_1");

        String id = service.initiateTransfer(validRequest);

        assertEquals("op_1", id);
        verify(logger).logTransfer(eq(validRequest), eq("op_1"), eq("PENDING"), eq(5.00));
    }

    @Test
    void initiateTransfer_ShouldThrow_WhenSenderNotFound() {
        when(commission.isValidAmount(any())).thenReturn(true);
        when(repository.cardExists("1111222233334444")).thenReturn(false);
        assertThrows(InvalidInputException.class, () -> service.initiateTransfer(validRequest));
    }

    @Test
    void initiateTransfer_ShouldThrow_WhenReceiverNotFound() {
        when(commission.isValidAmount(any())).thenReturn(true);
        when(repository.cardExists("1111222233334444")).thenReturn(true);
        when(repository.cardExists("5555666677778888")).thenReturn(false);
        assertThrows(InvalidInputException.class, () -> service.initiateTransfer(validRequest));
    }

    @Test
    void initiateTransfer_ShouldThrow_WhenInsufficientFunds() {
        when(commission.isValidAmount(any())).thenReturn(true);
        when(repository.cardExists(anyString())).thenReturn(true);
        when(commission.calculate(any())).thenReturn(new BigDecimal("5.00"));
        when(repository.hasSufficientFunds(eq("1111222233334444"), any())).thenReturn(false);
        assertThrows(TransferException.class, () -> service.initiateTransfer(validRequest));
    }

    @Test
    void confirmOperation_ShouldSucceed() {
        TransferRequest req = validRequest;
        when(repository.getOperation("op_1")).thenReturn(req);
        when(repository.getStatus("op_1")).thenReturn("PENDING");
        when(commission.calculate(new BigDecimal("500.00"))).thenReturn(new BigDecimal("5.00"));
        when(repository.hasSufficientFunds(eq("1111222233334444"), eq(new BigDecimal("50500")))).thenReturn(true);
        when(repository.withdraw("1111222233334444", new BigDecimal("50500"))).thenReturn(true);
        when(repository.deposit("5555666677778888", new BigDecimal("50000"))).thenReturn(true);

        String id = service.confirmOperation("op_1", "0000");

        assertEquals("op_1", id);
        verify(repository).updateStatus("op_1", "SUCCESS");
        verify(logger).logTransfer(eq(req), eq("op_1"), eq("SUCCESS"), eq(5.00));
    }

    @Test
    void confirmOperation_ShouldFail_WhenInvalidCode() {
        assertThrows(InvalidInputException.class, () -> service.confirmOperation("op_1", "1234"));
    }

    @Test
    void confirmOperation_ShouldFail_WhenOperationNotFound() {
        when(repository.getOperation("op_1")).thenReturn(null);
        assertThrows(InvalidInputException.class, () -> service.confirmOperation("op_1", "0000"));
    }

    @Test
    void confirmOperation_ShouldFail_WhenWithdrawFails() {
        TransferRequest req = validRequest;
        when(repository.getOperation("op_1")).thenReturn(req);
        when(repository.getStatus("op_1")).thenReturn("PENDING");
        when(commission.calculate(any())).thenReturn(new BigDecimal("5.00"));
        when(repository.hasSufficientFunds(anyString(), any())).thenReturn(true);
        when(repository.withdraw(anyString(), any())).thenReturn(false);

        assertThrows(TransferException.class, () -> service.confirmOperation("op_1", "0000"));
        verify(repository).updateStatus("op_1", "FAILED");
    }

    @Test
    void confirmOperation_ShouldRollback_WhenDepositFails() {
        TransferRequest req = validRequest;
        when(repository.getOperation("op_1")).thenReturn(req);
        when(repository.getStatus("op_1")).thenReturn("PENDING");
        when(commission.calculate(any())).thenReturn(new BigDecimal("5.00"));
        when(repository.hasSufficientFunds(anyString(), any())).thenReturn(true);
        when(repository.withdraw("1111222233334444", new BigDecimal("50500"))).thenReturn(true);
        when(repository.deposit("5555666677778888", new BigDecimal("50000"))).thenReturn(false);

        assertThrows(TransferException.class, () -> service.confirmOperation("op_1", "0000"));
        verify(repository).deposit("1111222233334444", new BigDecimal("50500"));
        verify(repository).updateStatus("op_1", "FAILED");
    }
}