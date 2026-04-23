package ru.netology.model.cards;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    private static final Currency RUB = Currency.getInstance("RUB");

    // Анонимная реализация для тестов
    private Card createCard(String number, BigDecimal balance, MoneyUnit scale) {
        return new DebitCard(number, "Test", "000", "00/00", balance, RUB, scale);
    }

    @Test
    void take_ShouldSucceed_WhenSufficientFunds() {
        Card card = createCard("1", new BigDecimal("1000.00"), MoneyUnit.RUBLES);
        assertTrue(card.take(new BigDecimal("10000"))); // 100 рублей
        assertEquals(new BigDecimal("900.00"), card.getBalance());
    }

    @Test
    void take_ShouldFail_WhenInsufficientFunds() {
        Card card = createCard("1", new BigDecimal("1000.00"), MoneyUnit.RUBLES);
        assertFalse(card.take(new BigDecimal("999999")));
        assertEquals(new BigDecimal("1000.00"), card.getBalance());
    }

    @Test
    void add_ShouldIncreaseBalance() {
        Card card = createCard("1", new BigDecimal("1000.00"), MoneyUnit.RUBLES);
        card.add(new BigDecimal("50000")); // 500 рублей
        assertEquals(new BigDecimal("1500.00"), card.getBalance());
    }

    @Test
    void isBalanceBigger_ShouldReturnTrue_WhenEnough() {
        Card card = createCard("1", new BigDecimal("2000.00"), MoneyUnit.RUBLES);
        assertTrue(card.isBalanceBigger(new BigDecimal("100000"))); // 1000 руб
    }

    @Test
    void take_WithAbs_ShouldConvertNegativeAmountToPositive() { // демонстрирует поведение abs()
        Card card = createCard("1", new BigDecimal("1000.00"), MoneyUnit.RUBLES);
        // Передадим отрицательную сумму, abs превратит её в положительную и спишет
        assertTrue(card.take(new BigDecimal("-10000"))); // 100 рублей
        assertEquals(new BigDecimal("900.00"), card.getBalance());
    }
}