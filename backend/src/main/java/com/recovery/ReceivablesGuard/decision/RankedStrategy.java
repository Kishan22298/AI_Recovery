package com.recovery.ReceivablesGuard.decision;

import java.math.BigDecimal;

public record RankedStrategy(
        InterventionStrategy strategy,
        BigDecimal probabilityOfSuccess,
        BigDecimal outstandingAmount,
        BigDecimal interventionCost,
        BigDecimal expectedValue,
        int rank
) {
}