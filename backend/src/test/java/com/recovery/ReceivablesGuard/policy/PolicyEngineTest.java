package com.recovery.ReceivablesGuard.policy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.recovery.ReceivablesGuard.observation.InvoiceContext;

class PolicyEngineTest {

    private PolicyEngine policyEngine;

    private final LocalDateTime businessTime =
            LocalDateTime.of(
                    2026,
                    9,
                    4,
                    12,
                    0
            );

    @BeforeEach
    void setUp() {

        policyEngine =
                new PolicyEngine(
                        new PolicyConfiguration()
                );
    }

    @Test
    void allowedStrategyIsAllowed() {

        InvoiceContext context =
                normalContext();

        PolicyResult result =
                policyEngine.authorize(
                        "EMAIL_REMINDER",
                        context,
                        businessTime
                );

        assertEquals(
                PolicyDecision.ALLOWED,
                result.decision()
        );
    }

    @Test
    void doNotContactBlocksStrategy() {

        InvoiceContext context =
                context(
                        true,
                        false,
                        1,
                        BigDecimal.valueOf(5000),
                        null
                );

        PolicyResult result =
                policyEngine.authorize(
                        "EMAIL_REMINDER",
                        context,
                        businessTime
                );

        assertEquals(
                PolicyDecision.BLOCKED,
                result.decision()
        );

        assertTrue(
                result.blockedRules()
                        .contains(
                                PolicyRule.DO_NOT_CONTACT
                        )
        );
    }

    @Test
    void brokenPromiseBlocksStrategy() {

        InvoiceContext context =
                context(
                        false,
                        true,
                        1,
                        BigDecimal.valueOf(5000),
                        null
                );

        PolicyResult result =
                policyEngine.authorize(
                        "EMAIL_REMINDER",
                        context,
                        businessTime
                );

        assertEquals(
                PolicyDecision.BLOCKED,
                result.decision()
        );

        assertTrue(
                result.blockedRules()
                        .contains(
                                PolicyRule.BROKEN_PROMISE_LOCKOUT
                        )
        );
    }

    @Test
    void contactCapAtBoundaryIsAllowed() {

        InvoiceContext context =
                context(
                        false,
                        false,
                        3,
                        BigDecimal.valueOf(5000),
                        null
                );

        PolicyResult result =
                policyEngine.authorize(
                        "EMAIL_REMINDER",
                        context,
                        businessTime
                );

        assertEquals(
                PolicyDecision.ALLOWED,
                result.decision()
        );
    }

    @Test
    void contactCapAboveBoundaryIsBlocked() {

        InvoiceContext context =
                context(
                        false,
                        false,
                        4,
                        BigDecimal.valueOf(5000),
                        null
                );

        PolicyResult result =
                policyEngine.authorize(
                        "EMAIL_REMINDER",
                        context,
                        businessTime
                );

        assertEquals(
                PolicyDecision.BLOCKED,
                result.decision()
        );

        assertTrue(
                result.blockedRules()
                        .contains(
                                PolicyRule.CONTACT_CAP
                        )
        );
    }

    @Test
    void amountAtThresholdIsAllowed() {

        InvoiceContext context =
                context(
                        false,
                        false,
                        1,
                        new BigDecimal("1000000.00"),
                        null
                );

        PolicyResult result =
                policyEngine.authorize(
                        "EMAIL_REMINDER",
                        context,
                        businessTime
                );

        assertEquals(
                PolicyDecision.ALLOWED,
                result.decision()
        );
    }

    @Test
    void amountAboveThresholdIsBlocked() {

        InvoiceContext context =
                context(
                        false,
                        false,
                        1,
                        new BigDecimal("1000000.01"),
                        null
                );

        PolicyResult result =
                policyEngine.authorize(
                        "EMAIL_REMINDER",
                        context,
                        businessTime
                );

        assertEquals(
                PolicyDecision.BLOCKED,
                result.decision()
        );

        assertTrue(
                result.blockedRules()
                        .contains(
                                PolicyRule.AMOUNT_THRESHOLD
                        )
        );
    }

    @Test
    void cooldownBlocksRecentContact() {

        LocalDateTime recentContact =
                businessTime.minusHours(1);

        InvoiceContext context =
                context(
                        false,
                        false,
                        1,
                        BigDecimal.valueOf(5000),
                        recentContact
                );

        PolicyResult result =
                policyEngine.authorize(
                        "EMAIL_REMINDER",
                        context,
                        businessTime
                );

        assertEquals(
                PolicyDecision.BLOCKED,
                result.decision()
        );

        assertTrue(
                result.blockedRules()
                        .contains(
                                PolicyRule.COOLDOWN
                        )
        );
    }

    @Test
    void cooldownAtBoundaryIsAllowed() {

        LocalDateTime lastContact =
                businessTime.minusHours(24);

        InvoiceContext context =
                context(
                        false,
                        false,
                        1,
                        BigDecimal.valueOf(5000),
                        lastContact
                );

        PolicyResult result =
                policyEngine.authorize(
                        "EMAIL_REMINDER",
                        context,
                        businessTime
                );

        assertEquals(
                PolicyDecision.ALLOWED,
                result.decision()
        );
    }

    @Test
    void outsideBusinessHoursIsBlocked() {

        LocalDateTime outsideHours =
                LocalDateTime.of(
                        2026,
                        9,
                        4,
                        22,
                        0
                );

        PolicyResult result =
                policyEngine.authorize(
                        "EMAIL_REMINDER",
                        normalContext(),
                        outsideHours
                );

        assertEquals(
                PolicyDecision.BLOCKED,
                result.decision()
        );

        assertTrue(
                result.blockedRules()
                        .contains(
                                PolicyRule.BUSINESS_HOURS
                        )
        );
    }

    @Test
    void safetyRestrictedStrategyIsBlocked() {

        PolicyResult result =
                policyEngine.authorize(
                        "LEGAL_THREAT",
                        normalContext(),
                        businessTime
                );

        assertEquals(
                PolicyDecision.BLOCKED,
                result.decision()
        );

        assertTrue(
                result.blockedRules()
                        .contains(
                                PolicyRule.SAFETY_RESTRICTION
                        )
        );
    }

    @Test
    void highestEvBlockedNextCandidateEvaluated() {

        InvoiceContext context =
                context(
                        true,
                        false,
                        1,
                        BigDecimal.valueOf(5000),
                        null
                );

        List<String> rankedStrategies =
                List.of(
                        "EMAIL_REMINDER",
                        "PHONE_CALL"
                );

        /*
         * Both strategies are evaluated through
         * exactly the same deterministic policy.
         *
         * Since DO_NOT_CONTACT is active,
         * neither can be authorized.
         */
        PolicyResult result =
                policyEngine.authorizeBestAvailable(
                        rankedStrategies,
                        context,
                        businessTime
                );

        assertEquals(
                PolicyDecision.HUMAN_ESCALATION,
                result.decision()
        );
    }

    @Test
    void allCandidatesBlockedMeansHumanEscalation() {

        InvoiceContext context =
                context(
                        true,
                        true,
                        10,
                        new BigDecimal("2000000"),
                        businessTime.minusHours(1)
                );

        List<String> candidates =
                List.of(
                        "EMAIL_REMINDER",
                        "PHONE_CALL",
                        "SMS"
                );

        PolicyResult result =
                policyEngine.authorizeBestAvailable(
                        candidates,
                        context,
                        businessTime
                );

        assertEquals(
                PolicyDecision.HUMAN_ESCALATION,
                result.decision()
        );

        assertNull(
                result.selectedStrategy()
        );
    }

    @Test
    void sameInputProducesSameResult() {

        InvoiceContext context =
                normalContext();

        PolicyResult first =
                policyEngine.authorize(
                        "EMAIL_REMINDER",
                        context,
                        businessTime
                );

        PolicyResult second =
                policyEngine.authorize(
                        "EMAIL_REMINDER",
                        context,
                        businessTime
                );

        assertEquals(
                first,
                second
        );
    }

    private InvoiceContext normalContext() {

        return context(
                false,
                false,
                1,
                BigDecimal.valueOf(5000),
                null
        );
    }

    /*
     * Adjust this constructor to the exact
     * InvoiceContext record used in your Phase 4.
     */
    private InvoiceContext context(
            boolean doNotContact,
            boolean brokenPromise,
            int contactFrequency,
            BigDecimal outstandingAmount,
            LocalDateTime lastContactAt
    ) {
        LocalDate today = LocalDate.now();
        return new InvoiceContext(
                "customer-1",           // invoiceReference
                "invoice-1",            // customerReference
                outstandingAmount,      // outstandingAmount
                today.minusDays(30),    // invoiceDate
                today,                  // dueDate
                30L,                    // invoiceAgeDays
                10L,                    // daysOverdue
                10,                     // historicalInvoiceCount
                8,                      // historicalPaidInvoiceCount
                2,                      // historicalLatePaymentCount
                BigDecimal.ZERO,        // historicalPaidAmount
                1,                      // paymentCount
                BigDecimal.ZERO,        // totalPaidAmount
                1,                      // communicationCount
                1,                      // successfulCommunicationCount
                0,                      // promiseCount
                0,                      // activePromiseCount
                brokenPromise ? 1 : 0,  // brokenPromiseCount
                0,                      // disputeCount
                0,                      // activeDisputeCount
                0,                      // previousInterventionCount
                0,                      // successfulInterventionCount
                0,                      // responseCount
                0,                      // contactCount
                0.8,                    // paymentHistoryRate
                0.8,                    // responsivenessRate
                0.0,                    // interventionSuccessRate
                0.0,                    // contactResponseRate
                true,                   // hasPaymentHistory
                true,                   // hasCommunicationHistory
                false,                  // hasPromiseHistory
                false,                  // hasDisputeHistory
                false,                  // hasInterventionHistory
                doNotContact,           // doNotContact
                contactFrequency,       // contactFrequency
                lastContactAt           // lastContactAt
        );
    }
}