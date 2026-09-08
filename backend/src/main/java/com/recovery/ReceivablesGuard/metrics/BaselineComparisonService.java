package com.recovery.ReceivablesGuard.metrics;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

@Service
public class BaselineComparisonService {

    private final RecoveryMetricsService recoveryMetricsService;

    public BaselineComparisonService(
            RecoveryMetricsService recoveryMetricsService) {

        this.recoveryMetricsService =
                recoveryMetricsService;
    }

    public BaselineComparison compare(
            BigDecimal agentRecoveredAmount,
            BigDecimal baselineRecoveredAmount) {

        if (agentRecoveredAmount == null ||
                baselineRecoveredAmount == null) {

            throw new IllegalArgumentException(
                    "Recovery amounts must not be null");
        }

        if (agentRecoveredAmount.signum() < 0 ||
                baselineRecoveredAmount.signum() < 0) {

            throw new IllegalArgumentException(
                    "Recovery amounts must not be negative");
        }

        BigDecimal incrementalRecovery =
                recoveryMetricsService
                        .calculateIncrementalRecovery(
                                agentRecoveredAmount,
                                baselineRecoveredAmount
                        );

        return new BaselineComparison(
                agentRecoveredAmount,
                baselineRecoveredAmount,
                incrementalRecovery,
                agentRecoveredAmount.compareTo(
                        baselineRecoveredAmount
                ) > 0
        );
    }
}