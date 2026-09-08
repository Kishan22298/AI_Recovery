package com.recovery.ReceivablesGuard.execution;

import java.math.BigDecimal;

public record ExecutionRequest(
        String executionId,
        String customerReference,
        String invoiceReference,
        String strategy,
        BigDecimal amount,
        boolean authorized,
        boolean simulationMode
) {
}