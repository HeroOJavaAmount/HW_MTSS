package ru.netology.model.cards;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public abstract class Card {
    protected final String number;
    protected final String name;
    protected final String cvv;
    protected final String validTill;
    protected BigDecimal balance;
    protected final Currency currency;
    protected final MoneyUnit scale;

    public Card(String number, String name, String cvv, String validTill,
                BigDecimal balance, Currency currency, MoneyUnit scale) {
        this.number = Objects.requireNonNull(number, "Card number must not be null-номер карты не должен быть null");
        this.name = name;
        this.cvv = cvv;
        this.validTill = validTill;
        this.balance = Objects.requireNonNull(balance, "Balance must not be null-баланс карты не должен быть пустым");
        this.currency = Objects.requireNonNull(currency, "Currency must not be null-валюта не должна быть пустой");
        this.scale = scale;
    }

    public MoneyUnit getScale() {
        return scale;
    }

    public Currency getCurrency() {
        return currency;
    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public boolean isBalanceBigger(BigDecimal amount) {
        if (amount != null) {
            amount = amount.movePointLeft(scale.getScale());}
            return balance.compareTo(amount) >= 0;
    }

    public boolean take(BigDecimal amount) {
        if (amount != null) {
            amount = amount.movePointLeft(scale.getScale()).abs();
        if (balance.compareTo(amount) >= 0) {
                balance = balance.subtract(amount);
                return true;
            }
        }
        return false;
    }

    public void add(BigDecimal amount) {
        if (amount != null) {
            amount = amount.movePointLeft(scale.getScale());
                    balance = balance.add(amount.abs());
        }else {throw new IllegalArgumentException("Сумма не должна быть null");}
    }
}
