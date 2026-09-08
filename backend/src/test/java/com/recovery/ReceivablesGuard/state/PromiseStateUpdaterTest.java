package com.recovery.ReceivablesGuard.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class PromiseStateUpdaterTest {

    private final PromiseStateUpdater updater =
            new PromiseStateUpdater();

    @Test
    void pendingCanBeFulfilled() {

        assertEquals(
                "FULFILLED",
                updater.transition(
                        "PENDING",
                        "FULFILLED"
                )
        );
    }

    @Test
    void pendingCanBeBroken() {

        assertEquals(
                "BROKEN",
                updater.transition(
                        "PENDING",
                        "BROKEN"
                )
        );
    }

    @Test
    void pendingCanBeCancelled() {

        assertEquals(
                "CANCELLED",
                updater.transition(
                        "PENDING",
                        "CANCELLED"
                )
        );
    }

    @Test
    void fulfilledCannotTransitionAgain() {

        assertThrows(
                InvalidStateTransitionException.class,
                () -> updater.transition(
                        "FULFILLED",
                        "BROKEN"
                )
        );
    }

    @Test
    void unknownTransitionIsRejected() {

        assertThrows(
                InvalidStateTransitionException.class,
                () -> updater.transition(
                        "PENDING",
                        "UNKNOWN"
                )
        );
    }
}