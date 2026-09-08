package com.recovery.ReceivablesGuard.event;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class AgentEventStreamServiceTest {

    @Test
    void subscribeCreatesEmitter() {

        AgentEventStreamService service =
                new AgentEventStreamService();

        SseEmitter emitter =
                service.subscribe(10L);

        assertNotNull(emitter);
    }

    @Test
    void subscribeRejectsNullRunId() {

        AgentEventStreamService service =
                new AgentEventStreamService();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.subscribe(null)
        );
    }

    @Test
    void publishWithoutSubscribersDoesNotFail() {

        AgentEventStreamService service =
                new AgentEventStreamService();

        AgentEvent event =
                new AgentEvent(
                        "event-1",
                        10L,
                        20L,
                        AgentEventType.RUN_STARTED,
                        Instant.now(),
                        Map.of()
                );

        assertDoesNotThrow(
                () -> service.publish(event)
        );
    }
}