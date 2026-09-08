package com.recovery.ReceivablesGuard.state;

import org.springframework.stereotype.Component;

@Component
public class PromiseStateUpdater {

    public String transition(
            String currentState,
            String event) {

        if (currentState == null || currentState.isBlank()) {
            throw new InvalidStateTransitionException(
                    "Current promise state is required"
            );
        }

        if (event == null || event.isBlank()) {
            throw new InvalidStateTransitionException(
                    "Promise event is required"
            );
        }

        return switch (currentState) {

            case "PENDING" -> switch (event) {
                case "FULFILLED" -> "FULFILLED";
                case "BROKEN" -> "BROKEN";
                case "CANCELLED" -> "CANCELLED";
                default -> throw invalid(currentState, event);
            };

            case "FULFILLED",
                 "BROKEN",
                 "CANCELLED" ->
                    throw invalid(currentState, event);

            default ->
                    throw new InvalidStateTransitionException(
                            "Unknown promise state: " + currentState
                    );
        };
    }

    private InvalidStateTransitionException invalid(
            String state,
            String event) {

        return new InvalidStateTransitionException(
                "Invalid promise transition: "
                        + state + " -> " + event
        );
    }
}