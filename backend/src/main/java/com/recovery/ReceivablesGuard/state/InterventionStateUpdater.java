package com.recovery.ReceivablesGuard.state;

import org.springframework.stereotype.Component;

@Component
public class InterventionStateUpdater {

    public String transition(
            String currentState,
            String event) {

        if (currentState == null || currentState.isBlank()) {
            throw new InvalidStateTransitionException(
                    "Current intervention state is required"
            );
        }

        if (event == null || event.isBlank()) {
            throw new InvalidStateTransitionException(
                    "Intervention event is required"
            );
        }

        return switch (currentState) {

            case "AUTHORIZED" -> switch (event) {
                case "EXECUTED" -> "EXECUTED";
                case "FAILED" -> "FAILED";
                default -> throw invalid(currentState, event);
            };

            case "EXECUTED",
                 "FAILED" ->
                    throw invalid(currentState, event);

            default ->
                    throw new InvalidStateTransitionException(
                            "Unknown intervention state: "
                                    + currentState
                    );
        };
    }

    private InvalidStateTransitionException invalid(
            String state,
            String event) {

        return new InvalidStateTransitionException(
                "Invalid intervention transition: "
                        + state + " -> " + event
        );
    }
}