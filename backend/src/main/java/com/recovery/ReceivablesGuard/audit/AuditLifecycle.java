package com.recovery.ReceivablesGuard.audit;

import org.springframework.stereotype.Component;

/**
 * Centralized audit lifecycle writer.
 *
 * Keeps orchestration code readable and guarantees that all
 * lifecycle events use the same audit service.
 */
@Component
public class AuditLifecycle {

    private static final String SYSTEM = "SYSTEM";

    private final AuditService auditService;

    public AuditLifecycle(AuditService auditService) {
        this.auditService = auditService;
    }

    public void observedContext(
            Long runId,
            Long roundId,
            String data
    ) {
        append(
                runId,
                roundId,
                AuditEventType.OBSERVED_CONTEXT,
                data
        );
    }

    public void diagnosis(
            Long runId,
            Long roundId,
            String data
    ) {
        append(
                runId,
                roundId,
                AuditEventType.DIAGNOSIS,
                data
        );
    }

    public void evidence(
            Long runId,
            Long roundId,
            String data
    ) {
        append(
                runId,
                roundId,
                AuditEventType.EVIDENCE,
                data
        );
    }

    public void candidateStrategies(
            Long runId,
            Long roundId,
            String data
    ) {
        append(
                runId,
                roundId,
                AuditEventType.CANDIDATE_STRATEGIES,
                data
        );
    }

    public void propensity(
            Long runId,
            Long roundId,
            String data
    ) {
        append(
                runId,
                roundId,
                AuditEventType.PROPENSITY,
                data
        );
    }

    public void expectedValue(
            Long runId,
            Long roundId,
            String data
    ) {
        append(
                runId,
                roundId,
                AuditEventType.EXPECTED_VALUE,
                data
        );
    }

    public void ranking(
            Long runId,
            Long roundId,
            String data
    ) {
        append(
                runId,
                roundId,
                AuditEventType.RANKING,
                data
        );
    }

    public void policy(
            Long runId,
            Long roundId,
            String data
    ) {
        append(
                runId,
                roundId,
                AuditEventType.POLICY,
                data
        );
    }

    public void blockedActions(
            Long runId,
            Long roundId,
            String data
    ) {
        append(
                runId,
                roundId,
                AuditEventType.BLOCKED_ACTIONS,
                data
        );
    }

    public void finalDecision(
            Long runId,
            Long roundId,
            String data
    ) {
        append(
                runId,
                roundId,
                AuditEventType.FINAL_DECISION,
                data
        );
    }

    public void execution(
            Long runId,
            Long roundId,
            String data
    ) {
        append(
                runId,
                roundId,
                AuditEventType.EXECUTION,
                data
        );
    }

    public void outcome(
            Long runId,
            Long roundId,
            String data
    ) {
        append(
                runId,
                roundId,
                AuditEventType.OUTCOME,
                data
        );
    }

    public void stateUpdate(
            Long runId,
            Long roundId,
            String data
    ) {
        append(
                runId,
                roundId,
                AuditEventType.STATE_UPDATE,
                data
        );
    }

    private void append(
            Long runId,
            Long roundId,
            AuditEventType type,
            String data
    ) {
        auditService.append(
                runId,
                roundId,
                type,
                SYSTEM,
                data
        );
    }
}