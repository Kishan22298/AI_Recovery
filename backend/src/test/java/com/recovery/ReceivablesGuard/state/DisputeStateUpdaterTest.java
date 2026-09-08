package com.recovery.ReceivablesGuard.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class DisputeStateUpdaterTest {

    private final DisputeStateUpdater updater =
            new DisputeStateUpdater();

    @Test
    void openCanBeResolved() {

        assertEquals(
                "RESOLVED",
                updater.transition(
                        "OPEN",
                        "RESOLVED"
                )
        );
    }

    @Test
    void openCanBeRejected() {

        assertEquals(
                "REJECTED",
                updater.transition(
                        "OPEN",
                        "REJECTED"
                )
        );
    }

    @Test
    void resolvedCannotChange() {

        assertThrows(
                InvalidStateTransitionException.class,
                () -> updater.transition(
                        "RESOLVED",
                        "REJECTED"
                )
        );
    }
}