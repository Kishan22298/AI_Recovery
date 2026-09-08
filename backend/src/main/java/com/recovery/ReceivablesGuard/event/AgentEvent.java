package com.recovery.ReceivablesGuard.event;

import java.time.Instant;
import java.util.Map;

public record AgentEvent(
        String eventId,
        Long agentRunId,
        Long roundId,
        AgentEventType eventType,
        Instant timestamp,
        Map<String, Object> payload
) {

    public AgentEvent {

        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException(
                    "eventId must not be blank"
            );
        }

        if (agentRunId == null) {
            throw new IllegalArgumentException(
                    "agentRunId must not be null"
            );
        }

        if (eventType == null) {
            throw new IllegalArgumentException(
                    "eventType must not be null"
            );
        }

        if (timestamp == null) {
            throw new IllegalArgumentException(
                    "timestamp must not be null"
            );
        }

        payload = payload == null
                ? Map.of()
                : Map.copyOf(payload);
    }
}