package com.recovery.ReceivablesGuard.metrics;

import java.math.BigDecimal;

public record BaselineResult(
        String invoiceReference,
        BigDecimal originalAmount,
        BigDecimal outstandingAmount,
        BigDecimal recoveredAmount,
        boolean interventionTaken
) {
}