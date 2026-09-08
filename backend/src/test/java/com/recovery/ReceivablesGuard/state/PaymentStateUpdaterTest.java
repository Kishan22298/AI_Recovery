package com.recovery.ReceivablesGuard.state;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class PaymentStateUpdaterTest {

    private final PaymentStateUpdater updater =
            new PaymentStateUpdater();

    @Test
    void partialPayment() {

        BigDecimal result =
                updater.calculateRemainingOutstanding(
                        new BigDecimal("100000"),
                        new BigDecimal("30000")
                );

        assertEquals(
                new BigDecimal("70000"),
                result
        );
    }

    @Test
    void fullPayment() {

        BigDecimal result =
                updater.calculateRemainingOutstanding(
                        new BigDecimal("100000"),
                        new BigDecimal("100000")
                );

        assertEquals(
                new BigDecimal("0"),
                result
        );
    }

    @Test
    void zeroPayment() {

        BigDecimal result =
                updater.calculateRemainingOutstanding(
                        new BigDecimal("100000"),
                        BigDecimal.ZERO
                );

        assertEquals(
                new BigDecimal("100000"),
                result
        );
    }

    @Test
    void paymentGreaterThanOutstandingIsRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () -> updater.calculateRemainingOutstanding(
                        new BigDecimal("100000"),
                        new BigDecimal("100001")
                )
        );
    }

    @Test
    void negativePaymentIsRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () -> updater.calculateRemainingOutstanding(
                        new BigDecimal("100000"),
                        new BigDecimal("-1")
                )
        );
    }
}