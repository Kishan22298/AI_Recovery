package com.recovery.ReceivablesGuard.execution;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IdempotencyStoreTest {

    private IdempotencyStore store;

    @BeforeEach
    void setUp() {

        store = new IdempotencyStore();
    }

    @Test
    void firstExecutionCanBeRegistered() {

        assertTrue(
                store.markProcessed("execution-1")
        );
    }

    @Test
    void duplicateExecutionCannotBeRegistered() {

        assertTrue(
                store.markProcessed("execution-1")
        );

        assertFalse(
                store.markProcessed("execution-1")
        );
    }

    @Test
    void unknownExecutionIsNotProcessed() {

        assertFalse(
                store.hasProcessed("unknown")
        );
    }

    @Test
    void processedExecutionIsDetected() {

        store.markProcessed("execution-1");

        assertTrue(
                store.hasProcessed("execution-1")
        );
    }
}