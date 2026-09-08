package com.recovery.ReceivablesGuard.agent;

import com.recovery.ReceivablesGuard.decision.DecisionResult;
import com.recovery.ReceivablesGuard.diagnosis.DiagnosisResult;
import com.recovery.ReceivablesGuard.execution.ExecutionResult;
import com.recovery.ReceivablesGuard.outcome.OutcomeResult;
import com.recovery.ReceivablesGuard.policy.PolicyResult;
import com.recovery.ReceivablesGuard.state.StateUpdateResult;

public record AgentRunResult(

        Long agentRunId,

        Long agentRoundId,

        Long invoiceId,

        String status,

        DiagnosisResult diagnosis,

        DecisionResult decision,

        PolicyResult policy,

        ExecutionResult execution,

        OutcomeResult outcome,

        StateUpdateResult state

) {

    public static AgentRunResult completed(
            Long agentRunId,
            Long agentRoundId,
            Long invoiceId,
            DiagnosisResult diagnosis,
            DecisionResult decision,
            PolicyResult policy,
            ExecutionResult execution,
            OutcomeResult outcome,
            StateUpdateResult state) {

        return new AgentRunResult(
                agentRunId,
                agentRoundId,
                invoiceId,
                "COMPLETED",
                diagnosis,
                decision,
                policy,
                execution,
                outcome,
                state
        );
    }

    public static AgentRunResult blocked(
            Long agentRunId,
            Long agentRoundId,
            Long invoiceId,
            DiagnosisResult diagnosis,
            DecisionResult decision,
            PolicyResult policy
            ) {

        return new AgentRunResult(
                agentRunId,
                agentRoundId,
                invoiceId,
                "BLOCKED",
                diagnosis,
                decision,
                policy,
                null,
                null,
                null
        );
    }
    public boolean isBlocked() {
        return "BLOCKED".equals(status);
    }
}