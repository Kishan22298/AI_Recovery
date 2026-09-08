package com.recovery.ReceivablesGuard.decision;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.recovery.ReceivablesGuard.observation.InvoiceContext;
import com.recovery.ReceivablesGuard.propensity.PropensityResult;
import com.recovery.ReceivablesGuard.propensity.PropensityScorer;
import com.recovery.ReceivablesGuard.propensity.ScoreBreakdown;

@Service
public class DecisionEngine {

    private final PropensityScorer propensityScorer;
    private final ExpectedValueCalculator expectedValueCalculator;

    public DecisionEngine(
            PropensityScorer propensityScorer,
            ExpectedValueCalculator expectedValueCalculator
    ) {
        this.propensityScorer = propensityScorer;
        this.expectedValueCalculator = expectedValueCalculator;
    }

    public DecisionResult decide(
            InvoiceContext context
    ) {

        if (context == null) {
            throw new IllegalArgumentException(
                    "Invoice context cannot be null"
            );
        }

        PropensityResult propensityResult =
                propensityScorer.score(context);

        BigDecimal probability =
                propensityResult.probability();

        ScoreBreakdown scoreBreakdown =
                propensityResult.breakdown();

        BigDecimal outstandingAmount =
                context.outstandingAmount();

        List<RankedStrategy> strategies =
                new ArrayList<>();

        for (InterventionStrategy strategy :
                InterventionStrategy.values()) {

            BigDecimal cost =
                    InterventionCost.forStrategy(strategy);

            BigDecimal expectedValue =
                    expectedValueCalculator.calculate(
                            probability,
                            outstandingAmount,
                            cost
                    );

            strategies.add(
                    new RankedStrategy(
                            strategy,
                            probability,
                            outstandingAmount,
                            cost,
                            expectedValue,
                            0
                    )
            );
        }

        strategies.sort(
                Comparator
                        .comparing(
                                RankedStrategy::expectedValue
                        )
                        .reversed()
                        .thenComparing(
                                ranked ->
                                        ranked.strategy().ordinal()
                        )
        );

        List<RankedStrategy> rankedStrategies =
                new ArrayList<>();

        for (int i = 0;
             i < strategies.size();
             i++) {

            RankedStrategy strategy =
                    strategies.get(i);

            rankedStrategies.add(
                    new RankedStrategy(
                            strategy.strategy(),
                            strategy.probabilityOfSuccess(),
                            strategy.outstandingAmount(),
                            strategy.interventionCost(),
                            strategy.expectedValue(),
                            i + 1
                    )
            );
        }

        RankedStrategy selected =
                rankedStrategies.isEmpty()
                        ? null
                        : rankedStrategies.get(0);

        return new DecisionResult(
                selected,
                rankedStrategies
        );
    }
}