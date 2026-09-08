package com.recovery.ReceivablesGuard.event;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.verify;
import org.springframework.context.ApplicationEventPublisher;

class AgentEventPublisherTest {

    private ApplicationEventPublisher springPublisher;

    private AgentEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        springPublisher =
                org.mockito.Mockito.mock(
                        ApplicationEventPublisher.class
                );

        eventPublisher =
                new AgentEventPublisher(
                        springPublisher
                );
    }

    @Test
    void publishCreatesAndPublishesEvent() {

        AgentEvent event =
                eventPublisher.publish(
                        10L,
                        20L,
                        AgentEventType.OBSERVATION_CREATED,
                        Map.of(
                                "invoiceReference",
                                "INV-001"
                        )
                );

        assertNotNull(event.eventId());
        assertEquals(10L, event.agentRunId());
        assertEquals(20L, event.roundId());

        assertEquals(
                AgentEventType.OBSERVATION_CREATED,
                event.eventType()
        );

        assertEquals(
                "INV-001",
                event.payload()
                        .get("invoiceReference")
        );

        ArgumentCaptor<AgentEvent> captor =
                ArgumentCaptor.forClass(
                        AgentEvent.class
                );

        verify(springPublisher)
                .publishEvent(captor.capture());

        assertEquals(
                event.eventId(),
                captor.getValue().eventId()
        );
    }

    @Test
    void publishPreservesEmptyPayload() {

        AgentEvent event =
                eventPublisher.publish(
                        10L,
                        null,
                        AgentEventType.RUN_STARTED,
                        null
                );

        assertNotNull(event.payload());
        assertEquals(
                Map.of(),
                event.payload()
        );
    }
}