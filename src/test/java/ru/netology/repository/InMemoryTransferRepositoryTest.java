package ru.netology.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.netology.dto.TransferRequest;
import ru.netology.exception.InvalidInputException;
import ru.netology.model.cards.DebitCard;
import ru.netology.model.cards.MoneyUnit;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTransferRepositoryTest {

    private InMemoryTransferRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTransferRepository();
        repository.cards.clear();
        Currency rub = Currency.getInstance("RUB");
        repository.cards.put("1111222233334444", new DebitCard(
                "1111222233334444", "Alice", "111", "12/29",
                new BigDecimal("10000.00"), rub, MoneyUnit.RUBLES));
        repository.cards.put("5555666677778888", new DebitCard(
                "5555666677778888", "Bob", "222", "06/27",
                new BigDecimal("5000.00"), rub, MoneyUnit.RUBLES));
    }

    @Test
    void withdraw_ShouldSucceed_WhenEnoughFunds() {
        assertTrue(repository.withdraw("1111222233334444", new BigDecimal("100000"))); // 1000 руб
        assertEquals(new BigDecimal("9000.00"), repository.getCard("1111222233334444").getBalance());
    }

    @Test
    void withdraw_ShouldFail_WhenInsufficientFunds() {
        assertFalse(repository.withdraw("1111222233334444", new BigDecimal("999999999")));
        assertEquals(new BigDecimal("10000.00"), repository.getCard("1111222233334444").getBalance());
    }

    @Test
    void withdraw_ShouldFail_WhenCardNotFound() {
        assertFalse(repository.withdraw("0000", new BigDecimal("1000")));
    }

    @Test
    void deposit_ShouldSucceed_WhenCardExists() {
        assertTrue(repository.deposit("5555666677778888", new BigDecimal("20000")));
        assertEquals(new BigDecimal("5200.00"), repository.getCard("5555666677778888").getBalance());
    }

    @Test
    void deposit_ShouldFail_WhenCardNotFound() {
        assertFalse(repository.deposit("0000", new BigDecimal("1000")));
    }

    @Test
    void hasSufficientFunds_ShouldReturnTrue_WhenEnough() {
        assertTrue(repository.hasSufficientFunds("1111222233334444", new BigDecimal("500000")));
    }

    @Test
    void hasSufficientFunds_ShouldReturnFalse_WhenNotEnough() {
        assertFalse(repository.hasSufficientFunds("1111222233334444", new BigDecimal("999999999")));
    }

    @Test
    void hasSufficientFunds_ShouldThrow_WhenCardNotFound() {
        assertThrows(InvalidInputException.class,
                () -> repository.hasSufficientFunds("unknown", new BigDecimal("100")));
    }

    @Test
    void saveAndGetOperation_ShouldWork() {
        TransferRequest req = new TransferRequest(
                "1111222233334444", "12/25", "123", "5555666677778888",
                new TransferRequest.Amount(50000, "RUB"));
        String id = repository.saveOperation(req);
        assertEquals("PENDING", repository.getStatus(id));
        assertEquals(req, repository.getOperation(id));
    }

    @Test
    void updateStatus_ShouldChange() {
        TransferRequest req = new TransferRequest(
                "1111222233334444", "12/25", "123", "5555666677778888",
                new TransferRequest.Amount(100, "RUB"));
        String id = repository.saveOperation(req);
        repository.updateStatus(id, "SUCCESS");
        assertEquals("SUCCESS", repository.getStatus(id));
    }
}