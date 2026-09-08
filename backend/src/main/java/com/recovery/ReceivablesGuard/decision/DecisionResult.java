package com.recovery.ReceivablesGuard.decision;

import java.util.List;

public record DecisionResult(
        RankedStrategy selectedStrategy,
        List<RankedStrategy> rankedStrategies
) {

    public DecisionResult {

        rankedStrategies =
                List.copyOf(rankedStrategies);
    }
}