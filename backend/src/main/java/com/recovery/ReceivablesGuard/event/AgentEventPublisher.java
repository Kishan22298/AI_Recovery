package com.recovery.ReceivablesGuard.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class AgentEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public AgentEventPublisher(
            ApplicationEventPublisher eventPublisher
    ) {
        this.eventPublisher = eventPublisher;
    }

    public AgentEvent publish(
            Long agentRunId,
            Long roundId,
            AgentEventType eventType,
            Map<String, Object> payload
    ) {

        AgentEvent event = new AgentEvent(
                UUID.randomUUID().toString(),
                agentRunId,
                roundId,
                eventType,
                Instant.now(),
                payload
        );

        eventPublisher.publishEvent(event);

        return event;
    }
}