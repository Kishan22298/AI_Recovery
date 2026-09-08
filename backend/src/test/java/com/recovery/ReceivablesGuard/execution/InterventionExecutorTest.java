package com.recovery.ReceivablesGuard.execution;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InterventionExecutorTest {

    private InterventionExecutor executor;

    private IdempotencyStore store;

    @BeforeEach
    void setUp() {

        store = new IdempotencyStore();

        executor =
                new InterventionExecutor(
                        store
                );
    }

    @Test
    void authorizedExecutionSucceeds() {

        ExecutionRequest request =
                request(
                        "execution-1",
                        true,
                        false
                );

        ExecutionResult result =
                executor.execute(request);

        assertEquals(
                ExecutionStatus.EXECUTED,
                result.status()
        );
    }

    @Test
    void unauthorizedExecutionIsBlocked() {

        ExecutionRequest request =
                request(
                        "execution-2",
                        false,
                        false
                );

        ExecutionResult result =
                executor.execute(request);

        assertEquals(
                ExecutionStatus.BLOCKED,
                result.status()
        );

        assertFalse(
                store.hasProcessed(
                        "execution-2"
                )
        );
    }

    @Test
    void duplicateExecutionIsRejected() {

        ExecutionRequest request =
                request(
                        "execution-3",
                        true,
                        false
                );

        ExecutionResult first =
                executor.execute(request);

        ExecutionResult second =
                executor.execute(request);

        assertEquals(
                ExecutionStatus.EXECUTED,
                first.status()
        );

        assertEquals(
                ExecutionStatus.DUPLICATE,
                second.status()
        );
    }

    @Test
    void simulationModeHasNoExternalExecution() {

        ExecutionRequest request =
                request(
                        "execution-4",
                        true,
                        true
                );

        ExecutionResult result =
                executor.execute(request);

        assertEquals(
                ExecutionStatus.SIMULATED,
                result.status()
        );
    }

    @Test
    void unauthorizedExecutionDoesNotBecomeProcessed() {

        ExecutionRequest request =
                request(
                        "execution-5",
                        false,
                        true
                );

        ExecutionResult result =
                executor.execute(request);

        assertEquals(
                ExecutionStatus.BLOCKED,
                result.status()
        );

        assertFalse(
                store.hasProcessed(
                        "execution-5"
                )
        );
    }

    @Test
    void sameInputProducesSameResult() {

        ExecutionRequest firstRequest =
                request(
                        "execution-6",
                        false,
                        true
                );

        ExecutionRequest secondRequest =
                request(
                        "execution-6",
                        false,
                        true
                );

        ExecutionResult first =
                executor.execute(firstRequest);

        store.clear();

        ExecutionResult second =
                executor.execute(secondRequest);

        assertEquals(
                first,
                second
        );
    }

    private ExecutionRequest request(
            String executionId,
            boolean authorized,
            boolean simulationMode
    ) {

        return new ExecutionRequest(
                executionId,
                "customer-1",
                "invoice-1",
                "EMAIL_REMINDER",
                new BigDecimal("5000.00"),
                authorized,
                simulationMode
        );
    }
}