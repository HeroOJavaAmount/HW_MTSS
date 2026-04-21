package ru.netology.repository;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;
import ru.netology.exception.InvalidInputException;
import ru.netology.model.TransferRequest;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static ru.netology.config.ConstantContainer.ERROR_INVALID_CARD_DATA;
import static ru.netology.config.ConstantContainer.ERROR_OPERATION_NOT_FOUND;

@Repository
public class InMemoryTransferRepository {

    private final Map<String, BigDecimal> balances = new ConcurrentHashMap<>();

    private final Map<String, TransferRequest> operations = new ConcurrentHashMap<>();

    private final Map<String, String> statuses = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        // Тестовые карты
        balances.put("1111222233334444", new BigDecimal("1000.00"));
        balances.put("5555666677778888", new BigDecimal("50000.00"));
    }

    public boolean cardExists(String cardNumber) {
        return balances.containsKey(cardNumber);
    }

    public BigDecimal getBalance(String cardNumber) {
        return balances.get(cardNumber);
    }

    public void withdraw(String cardNumber, BigDecimal amount) {
        balances.computeIfPresent(cardNumber, (k, v) -> v.subtract(amount));
    }

    public void deposit(String cardNumber, BigDecimal amount) {
        balances.compute(cardNumber, (k, v) -> (v == null) ? amount : v.add(amount));
    }

    public boolean hasSufficientFunds(String cardNumber, BigDecimal required) {
        BigDecimal balance = balances.get(cardNumber);
        if (balance == null) {
            throw new InvalidInputException("Карта не найдена: " + maskCard(cardNumber), ERROR_INVALID_CARD_DATA);
        }
        return balance.compareTo(required) >= 0;
    }

    public String saveOperation(TransferRequest request) {
        String operationId = generateId();
        operations.put(operationId, request);
        statuses.put(operationId, "PENDING");
        return operationId;
    }

    public TransferRequest getOperation(String operationId) {
        return operations.get(operationId);
    }

    public String getStatus(String operationId) {
        return statuses.get(operationId);
    }

    public void updateStatus(String operationId, String status) {
        statuses.put(operationId, status);
    }

    public boolean operationExists(String operationId) {
        return operations.containsKey(operationId);
    }

    private String generateId() {
        return "op_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 10000);
    }

    private String maskCard(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        return "****" + cardNumber.substring(cardNumber.length() - 4);
    }
}