package com.recovery.ReceivablesGuard.metrics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class RecoveryMetricsService {

    private static final int RATE_SCALE = 6;

    /**
     * Calculate recovery metrics for a dataset.
     *
     * recoveryRate is returned as a decimal:
     *
     * 0.000000 = 0%
     * 0.500000 = 50%
     * 1.000000 = 100%
     */
    public RecoveryMetrics calculate(
            BigDecimal originalAmount,
            BigDecimal outstandingAmount,
            int interventionCount,
            int blockedCount,
            int escalations) {

        validateAmounts(
                originalAmount,
                outstandingAmount
        );

        validateCounts(
                interventionCount,
                blockedCount,
                escalations
        );

        BigDecimal recoveredAmount =
                originalAmount.subtract(outstandingAmount);

        BigDecimal recoveryRate =
                calculateRecoveryRate(
                        originalAmount,
                        recoveredAmount
                );

        return new RecoveryMetrics(
                outstandingAmount,
                recoveredAmount,
                recoveryRate,
                interventionCount,
                blockedCount,
                escalations,
                BigDecimal.ZERO,
                recoveredAmount,
                BigDecimal.ZERO
        );
    }

    /**
     * Calculate metrics for an empty dataset.
     */
    public RecoveryMetrics calculateEmpty() {
        return RecoveryMetrics.empty();
    }

    /**
     * Calculate recovery rate.
     */
    public BigDecimal calculateRecoveryRate(
            BigDecimal originalAmount,
            BigDecimal recoveredAmount) {

        if (originalAmount == null ||
                recoveredAmount == null) {

            throw new IllegalArgumentException(
                    "Amounts must not be null");
        }

        if (originalAmount.signum() < 0 ||
                recoveredAmount.signum() < 0) {

            throw new IllegalArgumentException(
                    "Amounts must not be negative");
        }

        if (originalAmount.signum() == 0) {
            return BigDecimal.ZERO;
        }

                if (recoveredAmount.signum() == 0) {
                        return BigDecimal.ZERO;
                }

        return recoveredAmount
                .divide(
                        originalAmount,
                        RATE_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    /**
     * Calculate incremental recovery:
     *
     * agent recovery - baseline recovery
     */
    public BigDecimal calculateIncrementalRecovery(
            BigDecimal agentRecoveredAmount,
            BigDecimal baselineRecoveredAmount) {

        if (agentRecoveredAmount == null ||
                baselineRecoveredAmount == null) {

            throw new IllegalArgumentException(
                    "Recovery amounts must not be null");
        }

        BigDecimal incrementalRecovery =
                agentRecoveredAmount.subtract(
                        baselineRecoveredAmount
                );

        return incrementalRecovery.signum() == 0
                ? BigDecimal.ZERO
                : incrementalRecovery;
    }

    /**
     * Calculate metrics for invoice-level baseline results.
     */
    public RecoveryMetrics calculateBaselineMetrics(
            List<BaselineResult> results) {

        if (results == null || results.isEmpty()) {
            return RecoveryMetrics.empty();
        }

        BigDecimal originalAmount = BigDecimal.ZERO;
        BigDecimal outstandingAmount = BigDecimal.ZERO;
        BigDecimal recoveredAmount = BigDecimal.ZERO;

        int interventionCount = 0;

        for (BaselineResult result : results) {

            if (result == null) {
                continue;
            }

            originalAmount =
                    originalAmount.add(
                            result.originalAmount()
                    );

            outstandingAmount =
                    outstandingAmount.add(
                            result.outstandingAmount()
                    );

            recoveredAmount =
                    recoveredAmount.add(
                            result.recoveredAmount()
                    );

            if (result.interventionTaken()) {
                interventionCount++;
            }
        }

        BigDecimal recoveryRate =
                calculateRecoveryRate(
                        originalAmount,
                        recoveredAmount
                );

        return new RecoveryMetrics(
                outstandingAmount,
                recoveredAmount,
                recoveryRate,
                interventionCount,
                0,
                0,
                BigDecimal.ZERO,
                recoveredAmount,
                BigDecimal.ZERO
        );
    }

    private void validateAmounts(
            BigDecimal originalAmount,
            BigDecimal outstandingAmount) {

        if (originalAmount == null ||
                outstandingAmount == null) {

            throw new IllegalArgumentException(
                    "Amounts must not be null");
        }

        if (originalAmount.signum() < 0 ||
                outstandingAmount.signum() < 0) {

            throw new IllegalArgumentException(
                    "Amounts must not be negative");
        }

        if (outstandingAmount.compareTo(originalAmount) > 0) {
            throw new IllegalArgumentException(
                    "Outstanding amount cannot exceed original amount");
        }
    }

    private void validateCounts(
            int interventionCount,
            int blockedCount,
            int escalations) {

        if (interventionCount < 0 ||
                blockedCount < 0 ||
                escalations < 0) {

            throw new IllegalArgumentException(
                    "Metric counts must not be negative");
        }
    }
}