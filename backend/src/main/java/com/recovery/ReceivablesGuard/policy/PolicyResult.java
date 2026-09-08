package com.recovery.ReceivablesGuard.policy;

import java.util.List;

public record PolicyResult(
        PolicyDecision decision,
        String selectedStrategy,
        List<PolicyRule> blockedRules,
        String reason
) {

    public PolicyResult {
        blockedRules = blockedRules == null
                ? List.of()
                : List.copyOf(blockedRules);
    }

    public static PolicyResult allowed(
            String strategy
    ) {
        return new PolicyResult(
                PolicyDecision.ALLOWED,
                strategy,
                List.of(),
                "Strategy authorized"
        );
    }

    public static PolicyResult blocked(
            String strategy,
            List<PolicyRule> rules,
            String reason
    ) {
        return new PolicyResult(
                PolicyDecision.BLOCKED,
                strategy,
                rules,
                reason
        );
    }

    public static PolicyResult humanEscalation(
            String reason
    ) {
        return new PolicyResult(
                PolicyDecision.HUMAN_ESCALATION,
                null,
                List.of(),
                reason
        );
    }
}