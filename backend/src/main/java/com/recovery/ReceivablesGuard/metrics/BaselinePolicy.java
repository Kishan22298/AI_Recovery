package com.recovery.ReceivablesGuard.metrics;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

@Service
public class BaselinePolicy {

    /**
     * Deterministic baseline policy.
     *
     * The baseline does not use:
     * - Gemini
     * - propensity
     * - expected value
     * - ranking
     * - randomness
     *
     * It simply applies the same deterministic rule to every
     * invoice that has an outstanding balance.
     */
    public BaselineResult evaluate(
            String invoiceReference,
            BigDecimal originalAmount,
            BigDecimal outstandingAmount) {

        if (invoiceReference == null || invoiceReference.isBlank()) {
            throw new IllegalArgumentException(
                    "Invoice reference must not be blank");
        }

        if (originalAmount == null || originalAmount.signum() < 0) {
            throw new IllegalArgumentException(
                    "Original amount must not be negative");
        }

        if (outstandingAmount == null || outstandingAmount.signum() < 0) {
            throw new IllegalArgumentException(
                    "Outstanding amount must not be negative");
        }

        if (outstandingAmount.compareTo(originalAmount) > 0) {
            throw new IllegalArgumentException(
                    "Outstanding amount cannot exceed original amount");
        }

        BigDecimal recoveredAmount =
                originalAmount.subtract(outstandingAmount);

        if (recoveredAmount.signum() == 0) {
            recoveredAmount = BigDecimal.ZERO;
        }

        /*
         * Deterministic baseline rule:
         *
         * If money is still outstanding, take one baseline intervention.
         * If nothing is outstanding, no intervention is required.
         */
        boolean interventionTaken =
                outstandingAmount.signum() > 0;

        return new BaselineResult(
                invoiceReference,
                originalAmount,
                outstandingAmount,
                recoveredAmount,
                interventionTaken
        );
    }
}