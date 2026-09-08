package com.recovery.ReceivablesGuard.api.dto;

import java.time.Instant;

import com.recovery.ReceivablesGuard.audit.AuditEvent;

public record EscalationResponse(
        Long auditEventId,
        Long agentRunId,
        Long agentRoundId,
        Long sequenceNumber,
        String decision,
        String reason,
        Instant createdAt
) {

    public static EscalationResponse from(
            AuditEvent event) {

        if (event == null) {
            throw new IllegalArgumentException(
                    "Audit event must not be null"
            );
        }

        String eventData = event.getEventData();

        return new EscalationResponse(
                event.getId(),
                event.getAgentRunId(),
                event.getAgentRoundId(),
                event.getSequenceNumber(),
                "HUMAN_ESCALATION",
                extractJsonString(
                        eventData,
                        "reason"
                ),
                event.getCreatedAt()
        );
    }

    private static String extractJsonString(
            String json,
            String field) {

        if (json == null || field == null) {
            return null;
        }

        String key =
                "\"" + field + "\":\"";

        int start =
                json.indexOf(key);

        if (start < 0) {
            return null;
        }

        start += key.length();

        int end =
                json.indexOf(
                        "\"",
                        start
                );

        if (end < 0) {
            return null;
        }

        return json.substring(
                start,
                end
        );
    }
}