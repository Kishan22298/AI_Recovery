package com.recovery.ReceivablesGuard.execution;

import org.springframework.stereotype.Service;

@Service
public class InterventionExecutor {

    private final IdempotencyStore idempotencyStore;

    public InterventionExecutor(
            IdempotencyStore idempotencyStore
    ) {

        this.idempotencyStore =
                idempotencyStore;
    }

    public ExecutionResult execute(
            ExecutionRequest request
    ) {

        if (!request.authorized()) {

            return ExecutionResult.blocked(
                    request,
                    "Execution rejected because policy authorization is absent"
            );
        }

        if (idempotencyStore.hasProcessed(
                request.executionId()
        )) {

            return ExecutionResult.duplicate(
                    request
            );
        }

        /*
         * Simulation mode intentionally produces
         * no external side effects.
         */
        if (request.simulationMode()) {

            idempotencyStore.markProcessed(
                    request.executionId()
            );

            return ExecutionResult.simulated(
                    request
            );
        }

        /*
         * Phase 9 does not connect to real
         * email/payment infrastructure.
         *
         * Real gateways belong in a later
         * production integration phase.
         */

        idempotencyStore.markProcessed(
                request.executionId()
        );

        return ExecutionResult.executed(
                request
        );
    }
}