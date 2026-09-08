package com.recovery.ReceivablesGuard.state;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class InvoiceStateUpdaterTest {

    private final InvoiceStateUpdater updater =
            new InvoiceStateUpdater();

    @Test
    void positiveOutstandingIsOutstanding() {

        assertEquals(
                "OUTSTANDING",
                updater.determineStatus(
                        new BigDecimal("100")
                )
        );
    }

    @Test
    void zeroOutstandingIsPaid() {

        assertEquals(
                "PAID",
                updater.determineStatus(
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void negativeOutstandingIsRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () -> updater.determineStatus(
                        new BigDecimal("-1")
                )
        );
    }
}