package com.recovery.ReceivablesGuard.metrics;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class BaselineComparisonServiceTest {

    private final BaselineComparisonService service =
            new BaselineComparisonService(
                    new RecoveryMetricsService()
            );

    @Test
    void agentOutperformsBaseline() {

        BaselineComparison result =
                service.compare(
                        new BigDecimal("800.00"),
                        new BigDecimal("500.00")
                );

        assertEquals(
                new BigDecimal("800.00"),
                result.agentRecoveredAmount()
        );

        assertEquals(
                new BigDecimal("500.00"),
                result.baselineRecoveredAmount()
        );

        assertEquals(
                new BigDecimal("300.00"),
                result.incrementalRecovery()
        );

        assertTrue(
                result.agentOutperformedBaseline()
        );
    }

    @Test
    void agentAndBaselineAreEqual() {

        BaselineComparison result =
                service.compare(
                        new BigDecimal("500.00"),
                        new BigDecimal("500.00")
                );

        assertEquals(
                BigDecimal.ZERO,
                result.incrementalRecovery()
        );

        assertFalse(
                result.agentOutperformedBaseline()
        );
    }

    @Test
    void agentUnderperformsBaseline() {

        BaselineComparison result =
                service.compare(
                        new BigDecimal("400.00"),
                        new BigDecimal("500.00")
                );

        assertEquals(
                new BigDecimal("-100.00"),
                result.incrementalRecovery()
        );

        assertFalse(
                result.agentOutperformedBaseline()
        );
    }

    @Test
    void zeroRecoveryComparison() {

        BaselineComparison result =
                service.compare(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                );

        assertEquals(
                BigDecimal.ZERO,
                result.incrementalRecovery()
        );

        assertFalse(
                result.agentOutperformedBaseline()
        );
    }
}