package com.recovery.ReceivablesGuard.api.dto;

import java.time.Instant;

import com.recovery.ReceivablesGuard.audit.AuditEvent;

public record AuditEventResponse(
        Long id,
        Long agentRunId,
        Long agentRoundId,
        Long sequenceNumber,
        String eventType,
        String actor,
        String eventData,
        Instant createdAt
) {

    public static AuditEventResponse from(AuditEvent event) {

        if (event == null) {
            throw new IllegalArgumentException(
                    "Audit event must not be null"
            );
        }

        return new AuditEventResponse(
                event.getId(),
                event.getAgentRunId(),
                event.getAgentRoundId(),
                event.getSequenceNumber(),
                event.getEventType() != null
                        ? event.getEventType().name()
                        : null,
                event.getActor(),
                event.getEventData(),
                event.getCreatedAt()
        );
    }
}