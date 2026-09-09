package com.recovery.ReceivablesGuard.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.recovery.ReceivablesGuard.domain.InterventionOutcome;

public record InterventionOutcomeResponse(
        Long id,
        Long agentRoundId,
        String outcomeType,
        BigDecimal recoveredAmount,
        String notes,
        Instant occurredAt
) {

    public static InterventionOutcomeResponse from(
            InterventionOutcome outcome) {

        if (outcome == null) {
            throw new IllegalArgumentException(
                    "Intervention outcome must not be null"
            );
        }

        return new InterventionOutcomeResponse(
                outcome.getId(),
                outcome.getAgentRound() != null
                        ? outcome.getAgentRound().getId()
                        : null,
                outcome.getOutcomeType() != null
                        ? outcome.getOutcomeType().name()
                        : null,
                outcome.getRecoveredAmount(),
                outcome.getNotes(),
                outcome.getOccurredAt()
        );
    }
}