package com.recovery.ReceivablesGuard.metrics;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class BaselinePolicyTest {

    private final BaselinePolicy policy =
            new BaselinePolicy();

    @Test
    void baselineIsDeterministic() {

        BaselineResult first =
                policy.evaluate(
                        "INV-001",
                        new BigDecimal("1000.00"),
                        new BigDecimal("600.00")
                );

        BaselineResult second =
                policy.evaluate(
                        "INV-001",
                        new BigDecimal("1000.00"),
                        new BigDecimal("600.00")
                );

        assertEquals(first, second);
    }

    @Test
    void outstandingInvoiceGetsBaselineIntervention() {

        BaselineResult result =
                policy.evaluate(
                        "INV-001",
                        new BigDecimal("1000.00"),
                        new BigDecimal("600.00")
                );

        assertEquals(
                new BigDecimal("400.00"),
                result.recoveredAmount()
        );

        assertTrue(
                result.interventionTaken()
        );
    }

    @Test
    void fullyRecoveredInvoiceGetsNoIntervention() {

        BaselineResult result =
                policy.evaluate(
                        "INV-001",
                        new BigDecimal("1000.00"),
                        BigDecimal.ZERO
                );

        assertEquals(
                new BigDecimal("1000.00"),
                result.recoveredAmount()
        );

        assertFalse(
                result.interventionTaken()
        );
    }

    @Test
    void zeroRecoveryIsHandled() {

        BaselineResult result =
                policy.evaluate(
                        "INV-001",
                        new BigDecimal("1000.00"),
                        new BigDecimal("1000.00")
                );

        assertEquals(
                BigDecimal.ZERO,
                result.recoveredAmount()
        );

        assertTrue(
                result.interventionTaken()
        );
    }
}