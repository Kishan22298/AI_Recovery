package com.recovery.ReceivablesGuard.execution;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SimulationExecutionServiceTest {

    private SimulationExecutionService service;

    @BeforeEach
    void setUp() {

        IdempotencyStore store =
                new IdempotencyStore();

        InterventionExecutor executor =
                new InterventionExecutor(
                        store
                );

        service =
                new SimulationExecutionService(
                        executor
                );
    }

    @Test
    void authorizedSimulationIsSimulated() {

        ExecutionRequest request =
                new ExecutionRequest(
                        "sim-1",
                        "customer-1",
                        "invoice-1",
                        "EMAIL_REMINDER",
                        new BigDecimal("1000.00"),
                        true,
                        true
                );

        ExecutionResult result =
                service.execute(request);

        assertEquals(
                ExecutionStatus.SIMULATED,
                result.status()
        );
    }

    @Test
    void unauthorizedSimulationIsBlocked() {

        ExecutionRequest request =
                new ExecutionRequest(
                        "sim-2",
                        "customer-1",
                        "invoice-1",
                        "EMAIL_REMINDER",
                        new BigDecimal("1000.00"),
                        false,
                        true
                );

        ExecutionResult result =
                service.execute(request);

        assertEquals(
                ExecutionStatus.BLOCKED,
                result.status()
        );
    }

    @Test
    void simulationAlwaysUsesSimulationMode() {

        ExecutionRequest request =
                new ExecutionRequest(
                        "sim-3",
                        "customer-1",
                        "invoice-1",
                        "EMAIL_REMINDER",
                        new BigDecimal("1000.00"),
                        true,
                        false
                );

        ExecutionResult result =
                service.execute(request);

        assertEquals(
                ExecutionStatus.SIMULATED,
                result.status()
        );
    }
}