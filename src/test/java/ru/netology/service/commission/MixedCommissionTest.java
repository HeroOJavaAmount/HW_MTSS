package ru.netology.service.commission;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class MixedCommissionTest {

    private final MixedCommission commission = new MixedCommission();

    @Test
    void calculate_ShouldReturnOnePercent() {
        assertEquals(new BigDecimal("10.00"), commission.calculate(new BigDecimal("1000.00")));
        assertEquals(new BigDecimal("0.00"), commission.calculate(new BigDecimal("0.00")));
    }

    @Test
    void isValidAmount_ShouldRespectLimits() {
        assertTrue(commission.isValidAmount(new BigDecimal("1.00")));
        assertTrue(commission.isValidAmount(new BigDecimal("1000000.00")));
        assertFalse(commission.isValidAmount(new BigDecimal("0.99")));
        assertFalse(commission.isValidAmount(new BigDecimal("1000000.01")));
    }
}