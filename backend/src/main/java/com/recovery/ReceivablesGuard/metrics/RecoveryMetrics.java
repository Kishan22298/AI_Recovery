package com.recovery.ReceivablesGuard.metrics;

import java.math.BigDecimal;

public record RecoveryMetrics(
        BigDecimal outstandingAmount,
        BigDecimal recoveredAmount,
        BigDecimal recoveryRate,
        int interventionCount,
        int blockedCount,
        int escalations,
        BigDecimal incrementalRecovery,
        BigDecimal agentRecoveredAmount,
        BigDecimal baselineRecoveredAmount
) {

    public static RecoveryMetrics empty() {
        return new RecoveryMetrics(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0,
                0,
                0,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }
}