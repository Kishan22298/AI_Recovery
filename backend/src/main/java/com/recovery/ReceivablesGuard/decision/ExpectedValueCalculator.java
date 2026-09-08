package com.recovery.ReceivablesGuard.decision;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

@Service
public class ExpectedValueCalculator {

    private static final int MONEY_SCALE = 2;

    public BigDecimal calculate(
            BigDecimal probabilityOfSuccess,
            BigDecimal outstandingAmount,
            BigDecimal interventionCost
    ) {

        if (probabilityOfSuccess == null) {
            throw new IllegalArgumentException(
                    "Probability cannot be null"
            );
        }

        if (outstandingAmount == null) {
            throw new IllegalArgumentException(
                    "Outstanding amount cannot be null"
            );
        }

        if (interventionCost == null) {
            throw new IllegalArgumentException(
                    "Intervention cost cannot be null"
            );
        }

        if (probabilityOfSuccess.compareTo(BigDecimal.ZERO) < 0
                || probabilityOfSuccess.compareTo(BigDecimal.ONE) > 0) {

            throw new IllegalArgumentException(
                    "Probability must be between 0 and 1"
            );
        }

        BigDecimal expectedRecovery =
                probabilityOfSuccess
                        .multiply(outstandingAmount);

        return expectedRecovery
                .subtract(interventionCost)
                .setScale(
                        MONEY_SCALE,
                        RoundingMode.HALF_UP
                );
    }
}