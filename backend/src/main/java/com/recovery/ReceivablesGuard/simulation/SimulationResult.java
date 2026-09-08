package com.recovery.ReceivablesGuard.simulation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record SimulationResult(
        long seed,
        SimulationScenario scenario,
        String customerReference,
        String invoiceReference,
        BigDecimal invoiceAmount,
        BigDecimal paidAmount,
        BigDecimal outstandingAmount,
        boolean responded,
        boolean promisedPayment,
        boolean promiseBroken,
        boolean escalated,
        boolean failed,
        Instant simulatedAt
) {

    public SimulationResult {

        Objects.requireNonNull(scenario);
        Objects.requireNonNull(customerReference);
        Objects.requireNonNull(invoiceReference);
        Objects.requireNonNull(invoiceAmount);
        Objects.requireNonNull(paidAmount);
        Objects.requireNonNull(outstandingAmount);
        Objects.requireNonNull(simulatedAt);
    }
}