package com.recovery.ReceivablesGuard.outcome;

import java.math.BigDecimal;

public record OutcomeResult(

        OutcomeType outcomeType,

        BigDecimal invoiceAmount,

        BigDecimal paidAmount,

        BigDecimal remainingAmount,

        BigDecimal recoveryRate,

        boolean successful,

        boolean requiresFollowUp

) {

    public OutcomeResult {

        if (invoiceAmount == null) {
            throw new IllegalArgumentException(
                    "invoiceAmount must not be null"
            );
        }

        if (paidAmount == null) {
            throw new IllegalArgumentException(
                    "paidAmount must not be null"
            );
        }

        if (remainingAmount == null) {
            throw new IllegalArgumentException(
                    "remainingAmount must not be null"
            );
        }

        if (recoveryRate == null) {
            throw new IllegalArgumentException(
                    "recoveryRate must not be null"
            );
        }
    }
}