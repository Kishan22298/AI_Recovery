package com.recovery.ReceivablesGuard.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

class PolicyRuleTest {

    @Test
    void allPolicyRulesExist() {

        assertEquals(
                9,
                PolicyRule.values().length
        );
    }

    @Test
    void authorizationStatesExist() {

        assertNotNull(
                PolicyDecision.ALLOWED
        );

        assertNotNull(
                PolicyDecision.BLOCKED
        );

        assertNotNull(
                PolicyDecision.HUMAN_ESCALATION
        );
    }
}