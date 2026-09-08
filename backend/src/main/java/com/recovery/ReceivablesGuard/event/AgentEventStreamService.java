package com.recovery.ReceivablesGuard.event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class AgentEventStreamService {

    private static final long SSE_TIMEOUT_MILLIS = 30 * 60 * 1000L;

    private final Map<Long, List<SseEmitter>> emitters =
            new ConcurrentHashMap<>();

    private final Map<Long, List<AgentEvent>> eventHistory =
            new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long agentRunId) {

        if (agentRunId == null) {
            throw new IllegalArgumentException(
                    "agentRunId must not be null"
            );
        }

        SseEmitter emitter =
                new SseEmitter(SSE_TIMEOUT_MILLIS);

        List<AgentEvent> history =
                eventHistory.computeIfAbsent(
                        agentRunId,
                        ignored -> new ArrayList<>()
                );

        synchronized (history) {

            emitters.computeIfAbsent(
                    agentRunId,
                    ignored -> new CopyOnWriteArrayList<>()
            ).add(emitter);

            for (AgentEvent event : history) {
                sendEvent(emitter, event);
            }
        }

        emitter.onCompletion(
                () -> remove(agentRunId, emitter)
        );

        emitter.onTimeout(
                () -> remove(agentRunId, emitter)
        );

        emitter.onError(
                ignored -> remove(agentRunId, emitter)
        );

        return emitter;
    }

    public void publish(AgentEvent event) {

        if (event == null) {
            throw new IllegalArgumentException(
                    "event must not be null"
            );
        }

        List<AgentEvent> history =
                eventHistory.computeIfAbsent(
                        event.agentRunId(),
                        ignored -> new ArrayList<>()
                );

        synchronized (history) {

            history.add(event);

            List<SseEmitter> subscribers =
                    emitters.get(event.agentRunId());

            if (subscribers == null) {
                return;
            }

            for (SseEmitter emitter : subscribers) {
                sendEvent(emitter, event);
            }
        }
    }

    private void sendEvent(
            SseEmitter emitter,
            AgentEvent event
    ) {

        try {

            Map<String, Object> eventData =
                    new HashMap<>();

            eventData.put(
                    "eventId",
                    event.eventId()
            );

            eventData.put(
                    "agentRunId",
                    event.agentRunId()
            );

            eventData.put(
                    "roundId",
                    event.roundId()
            );

            eventData.put(
                    "eventType",
                    event.eventType().name()
            );

            eventData.put(
                    "timestamp",
                    event.timestamp()
            );

            eventData.put(
                    "payload",
                    event.payload()
            );

            emitter.send(
                    SseEmitter.event()
                            .id(event.eventId())
                            .name(event.eventType().name())
                            .data(eventData)
            );

        } catch (IOException exception) {
            remove(
                    event.agentRunId(),
                    emitter
            );
        }
    }

    private void remove(
            Long agentRunId,
            SseEmitter emitter
    ) {

        List<SseEmitter> subscribers =
                emitters.get(agentRunId);

        if (subscribers == null) {
            return;
        }

        subscribers.remove(emitter);

        if (subscribers.isEmpty()) {
            emitters.remove(agentRunId);
        }
    }
}