package ru.netology.service.commission;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class MixedCommissionTest {

    private final MixedCommission commission = new MixedCommission();

    @Test
    void calculate_ShouldReturnOnePercent() {
        BigDecimal result = commission.calculate(new BigDecimal("1000.00"));
        assertEquals(new BigDecimal("10.00"), result);
    }

    @Test
    void isValidAmount_ShouldRespectLimits() {
        // Лимиты сейчас 100 и 1_000_000_00 (копеек) = 1.00 и 1_000_000.00 рублей
        assertTrue(commission.isValidAmount(new BigDecimal("1.00")));
        assertTrue(commission.isValidAmount(new BigDecimal("1000000.00")));
        assertFalse(commission.isValidAmount(new BigDecimal("0.99")));
        assertFalse(commission.isValidAmount(new BigDecimal("1000000.01")));
    }
}