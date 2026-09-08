package com.recovery.ReceivablesGuard.state;

import org.springframework.stereotype.Component;

@Component
public class DisputeStateUpdater {

    public String transition(
            String currentState,
            String event) {

        if (currentState == null || currentState.isBlank()) {
            throw new InvalidStateTransitionException(
                    "Current dispute state is required"
            );
        }

        if (event == null || event.isBlank()) {
            throw new InvalidStateTransitionException(
                    "Dispute event is required"
            );
        }

        return switch (currentState) {

            case "OPEN" -> switch (event) {
                case "RESOLVED" -> "RESOLVED";
                case "REJECTED" -> "REJECTED";
                default -> throw invalid(currentState, event);
            };

            case "RESOLVED",
                 "REJECTED" ->
                    throw invalid(currentState, event);

            default ->
                    throw new InvalidStateTransitionException(
                            "Unknown dispute state: " + currentState
                    );
        };
    }

    private InvalidStateTransitionException invalid(
            String state,
            String event) {

        return new InvalidStateTransitionException(
                "Invalid dispute transition: "
                        + state + " -> " + event
        );
    }
}