package com.recovery.ReceivablesGuard.outcome;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class OutcomeEvaluatorTest {

    private final OutcomeEvaluator evaluator =
            new OutcomeEvaluator();

    private static final BigDecimal INVOICE =
            new BigDecimal("1000.00");

    @Test
    void paymentShouldRecoverFullInvoice() {

        OutcomeResult result =
                evaluator.evaluate(
                        OutcomeType.PAYMENT,
                        INVOICE,
                        new BigDecimal("1000.00")
                );

        assertEquals(
                new BigDecimal("1000.00"),
                result.paidAmount()
        );

        assertEquals(
                BigDecimal.ZERO.setScale(2),
                result.remainingAmount()
        );

        assertEquals(
                new BigDecimal("1.000000"),
                result.recoveryRate()
        );

        assertTrue(result.successful());
        assertFalse(result.requiresFollowUp());
    }

    @Test
    void partialPaymentShouldCalculateRemainingAmountCorrectly() {

        OutcomeResult result =
                evaluator.evaluate(
                        OutcomeType.PARTIAL_PAYMENT,
                        INVOICE,
                        new BigDecimal("375.25")
                );

        assertEquals(
                new BigDecimal("375.25"),
                result.paidAmount()
        );

        assertEquals(
                new BigDecimal("624.75"),
                result.remainingAmount()
        );

        assertEquals(
                new BigDecimal("0.375250"),
                result.recoveryRate()
        );

        assertTrue(result.successful());
        assertTrue(result.requiresFollowUp());
    }

    @Test
    void partialPaymentAtOneCentShouldBeHandledCorrectly() {

        OutcomeResult result =
                evaluator.evaluate(
                        OutcomeType.PARTIAL_PAYMENT,
                        INVOICE,
                        new BigDecimal("0.01")
                );

        assertEquals(
                new BigDecimal("0.01"),
                result.paidAmount()
        );

        assertEquals(
                new BigDecimal("999.99"),
                result.remainingAmount()
        );

        assertEquals(
                new BigDecimal("0.000010"),
                result.recoveryRate()
        );
    }

    @Test
    void partialPaymentJustBelowFullAmountShouldBeHandledCorrectly() {

        OutcomeResult result =
                evaluator.evaluate(
                        OutcomeType.PARTIAL_PAYMENT,
                        INVOICE,
                        new BigDecimal("999.99")
                );

        assertEquals(
                new BigDecimal("999.99"),
                result.paidAmount()
        );

        assertEquals(
                new BigDecimal("0.01"),
                result.remainingAmount()
        );

        assertEquals(
                new BigDecimal("0.999990"),
                result.recoveryRate()
        );
    }

    @Test
    void promiseShouldHaveNoPayment() {

        OutcomeResult result =
                evaluator.evaluate(
                        OutcomeType.PROMISE,
                        INVOICE,
                        BigDecimal.ZERO
                );

        assertEquals(
                BigDecimal.ZERO.setScale(2),
                result.paidAmount()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                result.remainingAmount()
        );

        assertEquals(
                BigDecimal.ZERO.setScale(6),
                result.recoveryRate()
        );

        assertTrue(result.successful());
        assertTrue(result.requiresFollowUp());
    }

    @Test
    void brokenPromiseShouldRequireFollowUp() {

        OutcomeResult result =
                evaluator.evaluate(
                        OutcomeType.BROKEN_PROMISE,
                        INVOICE,
                        BigDecimal.ZERO
                );

        assertFalse(result.successful());
        assertTrue(result.requiresFollowUp());
        assertEquals(
                new BigDecimal("1000.00"),
                result.remainingAmount()
        );
    }

    @Test
    void noResponseShouldHaveZeroRecovery() {

        OutcomeResult result =
                evaluator.evaluate(
                        OutcomeType.NO_RESPONSE,
                        INVOICE,
                        BigDecimal.ZERO
                );

        assertFalse(result.successful());
        assertTrue(result.requiresFollowUp());

        assertEquals(
                BigDecimal.ZERO.setScale(6),
                result.recoveryRate()
        );
    }

    @Test
    void disputeShouldHaveZeroRecovery() {

        OutcomeResult result =
                evaluator.evaluate(
                        OutcomeType.DISPUTE,
                        INVOICE,
                        BigDecimal.ZERO
                );

        assertFalse(result.successful());
        assertTrue(result.requiresFollowUp());

        assertEquals(
                new BigDecimal("1000.00"),
                result.remainingAmount()
        );
    }

    @Test
    void executionFailureShouldHaveZeroRecovery() {

        OutcomeResult result =
                evaluator.evaluate(
                        OutcomeType.EXECUTION_FAILURE,
                        INVOICE,
                        BigDecimal.ZERO
                );

        assertFalse(result.successful());
        assertTrue(result.requiresFollowUp());

        assertEquals(
                BigDecimal.ZERO.setScale(2),
                result.paidAmount()
        );
    }

    @Test
    void negativePaymentShouldBeRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () -> evaluator.evaluate(
                        OutcomeType.PAYMENT,
                        INVOICE,
                        new BigDecimal("-1.00")
                )
        );
    }

    @Test
    void zeroInvoiceShouldBeRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () -> evaluator.evaluate(
                        OutcomeType.PAYMENT,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void fullAmountCannotBeCalledPartialPayment() {

        assertThrows(
                IllegalArgumentException.class,
                () -> evaluator.evaluate(
                        OutcomeType.PARTIAL_PAYMENT,
                        INVOICE,
                        INVOICE
                )
        );
    }

    @Test
    void partialPaymentGreaterThanInvoiceShouldBeRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () -> evaluator.evaluate(
                        OutcomeType.PARTIAL_PAYMENT,
                        INVOICE,
                        new BigDecimal("1000.01")
                )
        );
    }

    @Test
    void sameInputShouldProduceSameResult() {

        OutcomeResult first =
                evaluator.evaluate(
                        OutcomeType.PARTIAL_PAYMENT,
                        INVOICE,
                        new BigDecimal("250.50")
                );

        OutcomeResult second =
                evaluator.evaluate(
                        OutcomeType.PARTIAL_PAYMENT,
                        INVOICE,
                        new BigDecimal("250.50")
                );

        assertEquals(first, second);
    }

    @Test
    void paidAmountShouldNeverExceedInvoiceAmount() {

        OutcomeResult result =
                evaluator.evaluate(
                        OutcomeType.PAYMENT,
                        INVOICE,
                        new BigDecimal("1500.00")
                );

        assertEquals(
                new BigDecimal("1000.00"),
                result.paidAmount()
        );

        assertEquals(
                BigDecimal.ZERO.setScale(2),
                result.remainingAmount()
        );
    }
}