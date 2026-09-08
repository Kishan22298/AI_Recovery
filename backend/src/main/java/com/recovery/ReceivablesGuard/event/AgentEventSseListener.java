package com.recovery.ReceivablesGuard.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AgentEventSseListener {

    private final AgentEventStreamService eventStreamService;

    public AgentEventSseListener(
            AgentEventStreamService eventStreamService
    ) {
        this.eventStreamService =
                eventStreamService;
    }

    @EventListener
    public void onAgentEvent(AgentEvent event) {

        eventStreamService.publish(event);
    }
}