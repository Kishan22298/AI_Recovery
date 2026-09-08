package com.recovery.ReceivablesGuard.metrics;

import java.math.BigDecimal;

public record BaselineComparison(
        BigDecimal agentRecoveredAmount,
        BigDecimal baselineRecoveredAmount,
        BigDecimal incrementalRecovery,
        boolean agentOutperformedBaseline
) {
}