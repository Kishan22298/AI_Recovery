package com.recovery.ReceivablesGuard.execution;

import org.springframework.stereotype.Service;

@Service
public class SimulationExecutionService {

    private final InterventionExecutor interventionExecutor;

    public SimulationExecutionService(
            InterventionExecutor interventionExecutor
    ) {

        this.interventionExecutor =
                interventionExecutor;
    }

    public ExecutionResult execute(
            ExecutionRequest request
    ) {

        /*
         * Simulation execution must never
         * perform a real external side effect.
         */

        ExecutionRequest simulationRequest =
                new ExecutionRequest(
                        request.executionId(),
                        request.customerReference(),
                        request.invoiceReference(),
                        request.strategy(),
                        request.amount(),
                        request.authorized(),
                        true
                );

        return interventionExecutor.execute(
                simulationRequest
        );
    }
}