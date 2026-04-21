package ru.netology.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.netology.exception.InvalidInputException;
import ru.netology.exception.TransferException;
import ru.netology.model.TransferRequest;
import ru.netology.repository.InMemoryTransferRepository;
import ru.netology.service.commission.MixedCommission;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

    @Mock
    private InMemoryTransferRepository repository;
    @Mock
    private MixedCommission commission;
    @Mock
    private LoggingService logger;
    @InjectMocks
    private TransferServiceImpl service;

    //Копейки-рубли 50000 копеек = 500 рублей
    private final TransferRequest validRequest = new TransferRequest(
            "1111222233334444", "12/25", "123", "5555666677778888",
            new TransferRequest.Amount(50000, "RUB")
    );

    @Test
    void initiateTransfer_ShouldSucceed_WhenValidData() {
        // amount в рублях = 500.00
        when(commission.isValidAmount(new BigDecimal("500.00"))).thenReturn(true);
        when(repository.cardExists("1111222233334444")).thenReturn(true);
        when(repository.cardExists("5555666677778888")).thenReturn(true);
        // комиссия 1% от 500 = 5.00
        when(commission.calculate(new BigDecimal("500.00"))).thenReturn(new BigDecimal("5.00"));
        when(repository.hasSufficientFunds(eq("1111222233334444"), eq(new BigDecimal("505.00")))).thenReturn(true);
        when(repository.saveOperation(validRequest)).thenReturn("op_123");

        String opId = service.initiateTransfer(validRequest);

        assertEquals("op_123", opId);
        verify(logger).logTransfer(eq(validRequest), eq("op_123"), eq("PENDING"), eq(5.00));
    }

    @Test
    void initiateTransfer_ShouldThrow_WhenSenderCardNotFound() {
        when(commission.isValidAmount(any(BigDecimal.class))).thenReturn(true);
        when(repository.cardExists("1111222233334444")).thenReturn(false);
        assertThrows(InvalidInputException.class, () -> service.initiateTransfer(validRequest));
        verify(repository, never()).saveOperation(any());
    }

    @Test
    void initiateTransfer_ShouldThrow_WhenReceiverCardNotFound() {
        when(commission.isValidAmount(any(BigDecimal.class))).thenReturn(true);
        when(repository.cardExists("1111222233334444")).thenReturn(true);
        when(repository.cardExists("5555666677778888")).thenReturn(false);
        assertThrows(InvalidInputException.class, () -> service.initiateTransfer(validRequest));
    }

    @Test
    void initiateTransfer_ShouldThrow_WhenInsufficientFunds() {
        when(commission.isValidAmount(any(BigDecimal.class))).thenReturn(true);
        when(repository.cardExists(anyString())).thenReturn(true);
        when(commission.calculate(any(BigDecimal.class))).thenReturn(new BigDecimal("5.00"));
        when(repository.hasSufficientFunds(eq("1111222233334444"), any(BigDecimal.class))).thenReturn(false);

        assertThrows(TransferException.class, () -> service.initiateTransfer(validRequest));
    }

    @Test
    void initiateTransfer_ShouldThrow_WhenSameCard() {
        TransferRequest sameCardRequest = new TransferRequest(
                "1111222233334444", "12/25", "123", "1111222233334444",
                new TransferRequest.Amount(50000, "RUB")
        );
        assertThrows(InvalidInputException.class, () -> service.initiateTransfer(sameCardRequest));
    }

    @Test
    void initiateTransfer_ShouldThrow_WhenUnsupportedCurrency() {
        TransferRequest badCurrency = new TransferRequest(
                "1111222233334444", "12/25", "123", "5555666677778888",
                new TransferRequest.Amount(50000, "USD")
        );
        assertThrows(InvalidInputException.class, () -> service.initiateTransfer(badCurrency));
    }

    @Test
    void initiateTransfer_ShouldThrow_WhenInvalidAmount() {
        when(commission.isValidAmount(any(BigDecimal.class))).thenReturn(false);
        assertThrows(InvalidInputException.class, () -> service.initiateTransfer(validRequest));
    }

    @Test
    void confirmOperation_ShouldSucceed_WhenValidCodeAndPending() {
        when(repository.getOperation("op_123")).thenReturn(validRequest);
        when(repository.getStatus("op_123")).thenReturn("PENDING");
        when(commission.calculate(new BigDecimal("500.00"))).thenReturn(new BigDecimal("5.00"));
        when(repository.hasSufficientFunds("1111222233334444", new BigDecimal("505.00"))).thenReturn(true);

        String result = service.confirmOperation("op_123", "0000");

        assertEquals("op_123", result);
        verify(repository).withdraw("1111222233334444", new BigDecimal("505.00"));
        verify(repository).deposit("5555666677778888", new BigDecimal("500.00"));
        verify(repository).updateStatus("op_123", "SUCCESS");
        verify(logger).logTransfer(eq(validRequest), eq("op_123"), eq("SUCCESS"), eq(5.00));
    }

    @Test
    void confirmOperation_ShouldThrow_WhenInvalidCode() {
        assertThrows(InvalidInputException.class, () -> service.confirmOperation("op_123", "9999"));
    }

    @Test
    void confirmOperation_ShouldThrow_WhenOperationNotFound() {
        when(repository.getOperation("op_123")).thenReturn(null);
        assertThrows(InvalidInputException.class, () -> service.confirmOperation("op_123", "0000"));
    }

    @Test
    void confirmOperation_ShouldThrow_WhenAlreadyProcessed() {
        when(repository.getOperation("op_123")).thenReturn(validRequest);
        when(repository.getStatus("op_123")).thenReturn("SUCCESS");
        assertThrows(InvalidInputException.class, () -> service.confirmOperation("op_123", "0000"));
    }

    @Test
    void confirmOperation_ShouldFailAndLog_WhenInsufficientFundsOnConfirmation() {
        when(repository.getOperation("op_123")).thenReturn(validRequest);
        when(repository.getStatus("op_123")).thenReturn("PENDING");
        when(commission.calculate(any(BigDecimal.class))).thenReturn(new BigDecimal("5.00"));
        when(repository.hasSufficientFunds(eq("1111222233334444"), any(BigDecimal.class))).thenReturn(false);

        assertThrows(TransferException.class, () -> service.confirmOperation("op_123", "0000"));
        verify(repository).updateStatus("op_123", "FAILED");
        verify(logger).logTransfer(eq(validRequest), eq("op_123"), eq("FAILED"), eq(5.00));
        verify(repository, never()).withdraw(any(), any());
    }
}