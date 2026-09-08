package com.recovery.ReceivablesGuard.metrics;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class RecoveryMetricsServiceTest {

    private final RecoveryMetricsService service =
            new RecoveryMetricsService();

    @Test
    void zeroRecovery() {

        RecoveryMetrics metrics =
                service.calculate(
                        new BigDecimal("1000.00"),
                        new BigDecimal("1000.00"),
                        0,
                        0,
                        0
                );

        assertEquals(
                new BigDecimal("1000.00"),
                metrics.outstandingAmount()
        );

        assertEquals(
                new BigDecimal("0.00"),
                metrics.recoveredAmount()
        );

        assertEquals(
                BigDecimal.ZERO,
                metrics.recoveryRate()
        );
    }

    @Test
    void fullRecovery() {

        RecoveryMetrics metrics =
                service.calculate(
                        new BigDecimal("1000.00"),
                        new BigDecimal("0.00"),
                        1,
                        0,
                        0
                );

        assertEquals(
                new BigDecimal("0.00"),
                metrics.outstandingAmount()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                metrics.recoveredAmount()
        );

        assertEquals(
                new BigDecimal("1.000000"),
                metrics.recoveryRate()
        );
    }

    @Test
    void partialRecovery() {

        RecoveryMetrics metrics =
                service.calculate(
                        new BigDecimal("1000.00"),
                        new BigDecimal("400.00"),
                        2,
                        1,
                        1
                );

        assertEquals(
                new BigDecimal("400.00"),
                metrics.outstandingAmount()
        );

        assertEquals(
                new BigDecimal("600.00"),
                metrics.recoveredAmount()
        );

        assertEquals(
                new BigDecimal("0.600000"),
                metrics.recoveryRate()
        );

        assertEquals(
                2,
                metrics.interventionCount()
        );

        assertEquals(
                1,
                metrics.blockedCount()
        );

        assertEquals(
                1,
                metrics.escalations()
        );
    }

    @Test
    void zeroOriginalAmountProducesZeroRecoveryRate() {

        RecoveryMetrics metrics =
                service.calculate(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        0,
                        0,
                        0
                );

        assertEquals(
                BigDecimal.ZERO,
                metrics.recoveryRate()
        );
    }

    @Test
    void incrementalRecoveryIsAgentMinusBaseline() {

        BigDecimal result =
                service.calculateIncrementalRecovery(
                        new BigDecimal("800.00"),
                        new BigDecimal("500.00")
                );

        assertEquals(
                new BigDecimal("300.00"),
                result
        );
    }

    @Test
    void negativeIncrementalRecoveryIsAllowed() {

        BigDecimal result =
                service.calculateIncrementalRecovery(
                        new BigDecimal("400.00"),
                        new BigDecimal("500.00")
                );

        assertEquals(
                new BigDecimal("-100.00"),
                result
        );
    }

    @Test
    void emptyDatasetReturnsZeroMetrics() {

        RecoveryMetrics metrics =
                service.calculateEmpty();

        assertEquals(
                BigDecimal.ZERO,
                metrics.outstandingAmount()
        );

        assertEquals(
                BigDecimal.ZERO,
                metrics.recoveredAmount()
        );

        assertEquals(
                BigDecimal.ZERO,
                metrics.recoveryRate()
        );

        assertEquals(
                0,
                metrics.interventionCount()
        );

        assertEquals(
                0,
                metrics.blockedCount()
        );

        assertEquals(
                0,
                metrics.escalations()
        );
    }
}