package com.recovery.ReceivablesGuard.propensity;

import java.math.BigDecimal;

public record ScoreBreakdown(
        BigDecimal baseScore,
        BigDecimal paymentHistoryAdjustment,
        BigDecimal responsivenessAdjustment,
        BigDecimal promiseAdjustment,
        BigDecimal disputeAdjustment,
        BigDecimal invoiceAgeAdjustment,
        BigDecimal outstandingAmountAdjustment,
        BigDecimal finalScore
) {
}