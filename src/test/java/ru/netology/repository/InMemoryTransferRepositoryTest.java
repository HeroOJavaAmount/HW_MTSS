package ru.netology.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.netology.exception.InvalidInputException;
import ru.netology.model.TransferRequest;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTransferRepositoryTest {

    private InMemoryTransferRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        repository = new InMemoryTransferRepository();
        Method initMethod = InMemoryTransferRepository.class.getDeclaredMethod("init");
        initMethod.setAccessible(true);
        initMethod.invoke(repository);
    }

    @Test
    void cardExists_ShouldReturnTrueForPredefinedCards() {
        assertTrue(repository.cardExists("1111222233334444"));
        assertTrue(repository.cardExists("5555666677778888"));
        assertFalse(repository.cardExists("0000000000000000"));
    }

    @Test
    void getBalance_ShouldReturnInitialBalance() {
        // Балансы в репозитории в рублях (1000.00 и 50000.00)
        assertEquals(new BigDecimal("1000.00"), repository.getBalance("1111222233334444"));
        assertEquals(new BigDecimal("50000.00"), repository.getBalance("5555666677778888"));
        assertNull(repository.getBalance("unknown"));
    }

    @Test
    void withdraw_ShouldDecreaseBalance() {
        repository.withdraw("1111222233334444", new BigDecimal("100.00"));
        assertEquals(new BigDecimal("900.00"), repository.getBalance("1111222233334444"));
    }

    @Test
    void deposit_ShouldIncreaseBalance() {
        repository.deposit("5555666677778888", new BigDecimal("200.00"));
        assertEquals(new BigDecimal("50200.00"), repository.getBalance("5555666677778888"));
    }

    @Test
    void deposit_ShouldCreateNewCardIfNotExists() {
        repository.deposit("9999999999999999", new BigDecimal("500.00"));
        assertTrue(repository.cardExists("9999999999999999"));
        assertEquals(new BigDecimal("500.00"), repository.getBalance("9999999999999999"));
    }

    @Test
    void hasSufficientFunds_ShouldReturnTrueWhenEnough() {
        assertTrue(repository.hasSufficientFunds("1111222233334444", new BigDecimal("1000.00")));
        assertTrue(repository.hasSufficientFunds("1111222233334444", new BigDecimal("500.00")));
    }

    @Test
    void hasSufficientFunds_ShouldReturnFalseWhenNotEnough() {
        assertFalse(repository.hasSufficientFunds("1111222233334444", new BigDecimal("1000.01")));
    }

    @Test
    void hasSufficientFunds_ShouldThrowWhenCardNotFound() {
        assertThrows(InvalidInputException.class, () ->
                repository.hasSufficientFunds("unknown", new BigDecimal("10"))
        );
    }

    @Test
    void saveOperation_ShouldReturnUniqueIdAndStoreRequest() {
        TransferRequest request = createValidRequest();
        String opId = repository.saveOperation(request);
        assertNotNull(opId);
        assertTrue(opId.startsWith("op_"));
        assertEquals(request, repository.getOperation(opId));
        assertEquals("PENDING", repository.getStatus(opId));
    }

    @Test
    void updateStatus_ShouldChangeStatus() {
        TransferRequest request = createValidRequest();
        String opId = repository.saveOperation(request);
        repository.updateStatus(opId, "SUCCESS");
        assertEquals("SUCCESS", repository.getStatus(opId));
    }

    @Test
    void operationExists_ShouldReturnTrueForSavedOperation() {
        TransferRequest request = createValidRequest();
        String opId = repository.saveOperation(request);
        assertTrue(repository.operationExists(opId));
        assertFalse(repository.operationExists("non-existent"));
    }

    private TransferRequest createValidRequest() {
        // 10000 копеек = 100 рублей
        return new TransferRequest(
                "1111222233334444",
                "12/25",
                "123",
                "5555666677778888",
                new TransferRequest.Amount(10000, "RUB")
        );
    }
}