package com.recovery.ReceivablesGuard.policy;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.recovery.ReceivablesGuard.observation.InvoiceContext;

@Service
public class PolicyEngine {

    private final PolicyConfiguration configuration;

    public PolicyEngine(
            PolicyConfiguration configuration
    ) {
        this.configuration = configuration;
    }

    public PolicyResult authorize(
            String strategy,
            InvoiceContext context,
            LocalDateTime now
    ) {

        List<PolicyRule> blockedRules =
                evaluateRules(strategy, context, now);

        if (blockedRules.isEmpty()) {
            return PolicyResult.allowed(strategy);
        }

        return PolicyResult.blocked(
                strategy,
                blockedRules,
                "Strategy blocked by deterministic policy"
        );
    }

    public PolicyResult authorizeBestAvailable(
            List<String> rankedStrategies,
            InvoiceContext context,
            LocalDateTime now
    ) {

        if (rankedStrategies == null ||
                rankedStrategies.isEmpty()) {

            return PolicyResult.humanEscalation(
                    "No candidate strategies available"
            );
        }

        for (String strategy : rankedStrategies) {

            PolicyResult result =
                    authorize(
                            strategy,
                            context,
                            now
                    );

            if (result.decision()
                    == PolicyDecision.ALLOWED) {

                return result;
            }
        }

        return PolicyResult.humanEscalation(
                "All candidate strategies blocked by policy"
        );
    }

    private List<PolicyRule> evaluateRules(
            String strategy,
            InvoiceContext context,
            LocalDateTime now
    ) {

        List<PolicyRule> blocked =
                new ArrayList<>();

        if (context == null) {
            blocked.add(
                    PolicyRule.SAFETY_RESTRICTION
            );

            return blocked;
        }

        if (context.outstandingAmount()
                == null ||
                context.outstandingAmount()
                        .compareTo(BigDecimal.ZERO) < 0) {

            blocked.add(
                    PolicyRule.SAFETY_RESTRICTION
            );
        }

        if (context.outstandingAmount()
                != null &&
                context.outstandingAmount()
                        .compareTo(
                                configuration
                                        .maximumAllowedAmount()
                        ) > 0) {

            blocked.add(
                    PolicyRule.AMOUNT_THRESHOLD
            );
        }

        if (context.doNotContact()) {

            blocked.add(
                    PolicyRule.DO_NOT_CONTACT
            );
        }

        if (context.hasBrokenPromise()) {

            blocked.add(
                    PolicyRule.BROKEN_PROMISE_LOCKOUT
            );
        }

        if (context.contactFrequency()
                > 0 &&
                context.contactFrequency()
                        > configuration.maxContactsPerDay()) {

            blocked.add(
                    PolicyRule.CONTACT_CAP
            );
        }

        if (context.lastContactAt()
                != null) {

            Duration elapsed =
                    Duration.between(
                            context.lastContactAt(),
                            now
                    );

            if (elapsed.compareTo(
                    configuration.cooldown()
            ) < 0) {

                blocked.add(
                        PolicyRule.COOLDOWN
                );
            }
        }

        if (!isBusinessHours(now.toLocalTime())) {

            blocked.add(
                    PolicyRule.BUSINESS_HOURS
            );
        }

        if (configuration.circuitBreakerEnabled()) {

            blocked.add(
                    PolicyRule.CIRCUIT_BREAKER
            );
        }

        if (isSafetyRestrictedStrategy(strategy)) {

            blocked.add(
                    PolicyRule.SAFETY_RESTRICTION
            );
        }

        return List.copyOf(blocked);
    }

    private boolean isBusinessHours(
            LocalTime time
    ) {

        return !time.isBefore(
                configuration.businessHoursStart()
        )
                &&
                !time.isAfter(
                        configuration.businessHoursEnd()
                );
    }

    private boolean isSafetyRestrictedStrategy(
            String strategy
    ) {

        if (strategy == null) {
            return true;
        }

        String normalized =
                strategy.trim().toUpperCase();

        return normalized.contains("LEGAL")
                || normalized.contains("THREAT")
                || normalized.contains("HARASS");
    }
}