package ru.netology.service.commission;


import java.math.BigDecimal;


public abstract class Commission {
    protected CommissionType type;

    public abstract void setYourCommission(CommissionType type);

    public abstract BigDecimal calculate(BigDecimal amount);

    public CommissionType getType() {
        return type;
    }
}
