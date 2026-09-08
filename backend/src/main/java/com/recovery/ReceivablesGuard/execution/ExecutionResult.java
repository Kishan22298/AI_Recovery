package com.recovery.ReceivablesGuard.execution;

public record ExecutionResult(
        String executionId,
        ExecutionStatus status,
        String strategy,
        String message
) {

    public static ExecutionResult executed(
            ExecutionRequest request
    ) {

        return new ExecutionResult(
                request.executionId(),
                ExecutionStatus.EXECUTED,
                request.strategy(),
                "Execution completed"
        );
    }

    public static ExecutionResult simulated(
            ExecutionRequest request
    ) {

        return new ExecutionResult(
                request.executionId(),
                ExecutionStatus.SIMULATED,
                request.strategy(),
                "Execution simulated; no external side effect"
        );
    }

    public static ExecutionResult blocked(
            ExecutionRequest request,
            String reason
    ) {

        return new ExecutionResult(
                request.executionId(),
                ExecutionStatus.BLOCKED,
                request.strategy(),
                reason
        );
    }

    public static ExecutionResult duplicate(
            ExecutionRequest request
    ) {

        return new ExecutionResult(
                request.executionId(),
                ExecutionStatus.DUPLICATE,
                request.strategy(),
                "Execution already processed"
        );
    }

    public static ExecutionResult failed(
            ExecutionRequest request,
            String reason
    ) {

        return new ExecutionResult(
                request.executionId(),
                ExecutionStatus.FAILED,
                request.strategy(),
                reason
        );
    }

    public static ExecutionResult timeout(
            ExecutionRequest request,
            String reason
    ) {

        return new ExecutionResult(
                request.executionId(),
                ExecutionStatus.TIMEOUT,
                request.strategy(),
                reason
        );
    }
}