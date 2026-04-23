package ru.netology.repository;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;
import ru.netology.exception.InvalidInputException;
import ru.netology.dto.TransferRequest;
import ru.netology.model.cards.Card;
import ru.netology.model.cards.DebitCard;
import ru.netology.model.cards.MoneyUnit;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static ru.netology.config.ConstantContainer.ERROR_INVALID_CARD_DATA;

@Repository
public class InMemoryTransferRepository {

    protected final Map<String, Card> cards = new ConcurrentHashMap<>();

    private final Map<String, TransferRequest> operations = new ConcurrentHashMap<>();

    private final Map<String, String> statuses = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        Currency rub = Currency.getInstance("RUB");
        // Создаём тестовые карты с балансом
        cards.put("1111222233334444", new DebitCard(
                "1111222233334444", "Ivan Ivanov","333","12/29",
                new BigDecimal("10000.00"), rub, MoneyUnit.RUBLES));
        cards.put("5555666677778888", new DebitCard(
                "5555666677778888", "Petr Petrov","555","11/28",
                new BigDecimal("5000.00"), rub, MoneyUnit.RUBLES));
    }

    public Card getCard(String cardNumber) {
        return cards.get(cardNumber);
    }

    public boolean cardExists(String cardNumber) {
        return cards.containsKey(cardNumber);
    }

    public boolean withdraw(String cardNumber, BigDecimal amount) {
        Card card = cards.get(cardNumber);
        if (card != null)return card.take(amount);
        return false;

    }

    public boolean deposit(String cardNumber, BigDecimal amount) {
        Card card = cards.get(cardNumber);
        if (card != null){ card.add(amount);
            return true;
        }
        return false;
    }

    public boolean hasSufficientFunds(String cardNumber, BigDecimal required) {
        Card card = cards.get(cardNumber);
        if (card == null) {
            throw new InvalidInputException("Карта не найдена: " + maskCard(cardNumber), ERROR_INVALID_CARD_DATA);
        }
        return card.isBalanceBigger(required);
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
    private String generateId() {
        return UUID.randomUUID().toString();
    }

    private String maskCard(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        return "****" + cardNumber.substring(cardNumber.length() - 4);
    }
}