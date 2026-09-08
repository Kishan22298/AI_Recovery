package com.recovery.ReceivablesGuard.api.dto;

import java.math.BigDecimal;

import com.recovery.ReceivablesGuard.metrics.RecoveryMetrics;

public record MetricsResponse(
        BigDecimal outstandingAmount,
        BigDecimal recoveredAmount,
        BigDecimal recoveryRate,
        int interventionCount,
        int blockedCount,
        int escalations,
        BigDecimal baselineRecoveredAmount,
        BigDecimal agentRecoveredAmount,
        BigDecimal incrementalRecovery
) {

    public static MetricsResponse from(
            RecoveryMetrics metrics) {

        if (metrics == null) {
            throw new IllegalArgumentException(
                    "Metrics must not be null"
            );
        }

        return new MetricsResponse(
                metrics.outstandingAmount(),
                metrics.recoveredAmount(),
                metrics.recoveryRate(),
                metrics.interventionCount(),
                metrics.blockedCount(),
                metrics.escalations(),
                metrics.baselineRecoveredAmount(),
                metrics.agentRecoveredAmount(),
                metrics.incrementalRecovery()
        );
    }
}