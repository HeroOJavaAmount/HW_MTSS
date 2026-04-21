package ru.netology.service.commission;

import org.springframework.stereotype.Service;
import ru.netology.config.ConstantContainer;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class MixedCommission extends Commission {
        private BigDecimal fixedPart;
        private BigDecimal partAsPercentage;

        public MixedCommission() {
            this.type = CommissionType.MIXED;
            this.fixedPart = BigDecimal.ZERO;
            this.partAsPercentage = BigDecimal.valueOf(1.0);
        }

    @Override
    public void setYourCommission(CommissionType type) {
        if (type != CommissionType.MIXED) {
            throw new IllegalArgumentException
                    ("Неверный тип комиссии: " + type);
        }
        this.type = type;
    }

    @Override
    public BigDecimal calculate(BigDecimal amount) {
        BigDecimal percentage = amount.multiply(partAsPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return percentage;
    }

    public boolean isValidAmount(BigDecimal amount) {
        return amount.compareTo(ConstantContainer.MIN_TRANSFER_AMOUNT) >= 0 &&
                amount.compareTo(ConstantContainer.MAX_TRANSFER_AMOUNT) <= 0;
    }

    public void setParts(BigDecimal fixed, BigDecimal percentage) {
        this.fixedPart = fixed;
        this.partAsPercentage = percentage;
    }
}
