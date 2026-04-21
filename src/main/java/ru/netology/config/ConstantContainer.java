package ru.netology.config;

import java.math.BigDecimal;

public class ConstantContainer {
    public static final String CURRENCY_RUB = "RUB";
    public static final String DEFAULT_VERIFICATION_CODE = "0000";
    public static final BigDecimal MAX_TRANSFER_AMOUNT = new BigDecimal("1000000.00");
    public static final BigDecimal MIN_TRANSFER_AMOUNT = new BigDecimal("1.00");

    public static final int ERROR_INVALID_CARD_DATA = 4000;
    public static final int ERROR_SAME_CARD = 4001;
    public static final int ERROR_INVALID_AMOUNT = 4002;
    public static final int ERROR_INVALID_CURRENCY = 4003;
    public static final int ERROR_OPERATION_NOT_FOUND = 4004;
    public static final int ERROR_INVALID_CODE = 4005;
    public static final int ERROR_TRANSFER_FAILED = 5000;
}