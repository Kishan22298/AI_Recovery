package com.recovery.ReceivablesGuard.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.recovery.ReceivablesGuard.domain.AgentRound;

public record AgentRoundResponse(
        Long id,
        Long agentRunId,
        Integer roundNumber,
        String status,
        String diagnosisCategory,
        BigDecimal propensityScore,
        String selectedStrategy,
        boolean authorized,
        Instant startedAt,
        Instant completedAt
) {

    public static AgentRoundResponse from(AgentRound round) {

        if (round == null) {
            throw new IllegalArgumentException(
                    "Agent round must not be null"
            );
        }

        return new AgentRoundResponse(
                round.getId(),
                round.getAgentRun() != null
                        ? round.getAgentRun().getId()
                        : null,
                round.getRoundNumber(),
                round.getStatus() != null
                        ? round.getStatus().name()
                        : null,
                round.getDiagnosisCategory(),
                round.getPropensityScore(),
                round.getSelectedStrategy(),
                round.isAuthorized(),
                round.getStartedAt(),
                round.getCompletedAt()
        );
    }
}