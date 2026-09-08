package com.recovery.ReceivablesGuard.api.dto;

import java.time.Instant;

import com.recovery.ReceivablesGuard.domain.AgentRun;

public record AgentRunResponse(
        Long id,
        Long invoiceId,
        String invoiceReference,
        String status,
        Integer maxRounds,
        Instant startedAt,
        Instant completedAt,
        Instant createdAt
) {

    public static AgentRunResponse from(AgentRun agentRun) {

        if (agentRun == null) {
            throw new IllegalArgumentException(
                    "Agent run must not be null"
            );
        }

        return new AgentRunResponse(
                agentRun.getId(),
                agentRun.getInvoice() != null
                        ? agentRun.getInvoice().getId()
                        : null,
                agentRun.getInvoice() != null
                        ? agentRun.getInvoice().getExternalRef()
                        : null,
                agentRun.getStatus() != null
                        ? agentRun.getStatus().name()
                        : null,
                agentRun.getMaxRounds(),
                agentRun.getStartedAt(),
                agentRun.getCompletedAt(),
                agentRun.getCreatedAt()
        );
    }
}