package com.recovery.ReceivablesGuard.decision;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

public final class InterventionCost {

    private static final Map<InterventionStrategy, BigDecimal> COSTS =
            new EnumMap<>(InterventionStrategy.class);

    static {
        COSTS.put(
                InterventionStrategy.EMAIL_REMINDER,
                new BigDecimal("5.00")
        );

        COSTS.put(
                InterventionStrategy.PHONE_CALL,
                new BigDecimal("25.00")
        );

        COSTS.put(
                InterventionStrategy.PAYMENT_PLAN,
                new BigDecimal("15.00")
        );

        COSTS.put(
                InterventionStrategy.DISPUTE_FOLLOW_UP,
                new BigDecimal("20.00")
        );

        COSTS.put(
                InterventionStrategy.ESCALATION,
                new BigDecimal("50.00")
        );
    }

    private InterventionCost() {
    }

    public static BigDecimal forStrategy(
            InterventionStrategy strategy
    ) {

        if (strategy == null) {
            throw new IllegalArgumentException(
                    "Strategy cannot be null"
            );
        }

        BigDecimal cost = COSTS.get(strategy);

        if (cost == null) {
            throw new IllegalArgumentException(
                    "No intervention cost configured for: " + strategy
            );
        }

        return cost;
    }
}