package com.recovery.ReceivablesGuard.api;

import com.recovery.ReceivablesGuard.event.AgentEventStreamService;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/agent-runs")
public class AgentEventController {

    private final AgentEventStreamService eventStreamService;

    public AgentEventController(
            AgentEventStreamService eventStreamService
    ) {
        this.eventStreamService =
                eventStreamService;
    }

    @GetMapping(
            value = "/{id}/events",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter events(
            @PathVariable Long id
    ) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Agent run ID must be positive"
            );
        }

        return eventStreamService.subscribe(id);
    }
}