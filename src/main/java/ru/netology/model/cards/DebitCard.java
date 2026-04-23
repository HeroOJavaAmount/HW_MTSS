package ru.netology.model.cards;

import java.math.BigDecimal;
import java.util.Currency;

public class DebitCard extends Card {
    public DebitCard(String number, String name, String cvv, String validTill,
                     BigDecimal balance, Currency currency, MoneyUnit scale) {
        super(number, name, cvv, validTill, balance, currency, scale);
    }
}

