package com.recovery.ReceivablesGuard.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class InterventionStateUpdaterTest {

    private final InterventionStateUpdater updater =
            new InterventionStateUpdater();

    @Test
    void authorizedCanExecute() {

        assertEquals(
                "EXECUTED",
                updater.transition(
                        "AUTHORIZED",
                        "EXECUTED"
                )
        );
    }

    @Test
    void authorizedCanFail() {

        assertEquals(
                "FAILED",
                updater.transition(
                        "AUTHORIZED",
                        "FAILED"
                )
        );
    }

    @Test
    void executedCannotExecuteAgain() {

        assertThrows(
                InvalidStateTransitionException.class,
                () -> updater.transition(
                        "EXECUTED",
                        "EXECUTED"
                )
        );
    }
}