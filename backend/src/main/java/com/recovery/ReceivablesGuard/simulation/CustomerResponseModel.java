package com.recovery.ReceivablesGuard.simulation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

/**
 * Deterministic customer response model.
 *
 * This class only simulates outcomes.
 *
 * It does NOT:
 * - send emails
 * - make payments
 * - call external APIs
 * - modify the database
 */
public class CustomerResponseModel {

    private static final int MONEY_SCALE = 2;

    public SimulationResult simulate(
            long seed,
            SimulationScenario scenario,
            String customerReference,
            String invoiceReference,
            BigDecimal invoiceAmount,
            SimulationClock clock
    ) {

        Random random = new Random(seed);

        BigDecimal paidAmount = BigDecimal.ZERO;

        boolean responded = false;
        boolean promisedPayment = false;
        boolean promiseBroken = false;
        boolean escalated = false;
        boolean failed = false;

        switch (scenario) {

            case PAYS -> {
                responded = true;

                paidAmount = invoiceAmount
                        .setScale(
                                MONEY_SCALE,
                                RoundingMode.HALF_UP
                        );
            }

            case PARTIAL_PAYS -> {
                responded = true;

                double percentage =
                        0.25 + (random.nextDouble() * 0.50);

                paidAmount = invoiceAmount
                        .multiply(BigDecimal.valueOf(percentage))
                        .setScale(
                                MONEY_SCALE,
                                RoundingMode.HALF_UP
                        );

                if (paidAmount.compareTo(invoiceAmount) > 0) {
                    paidAmount = invoiceAmount;
                }
            }

            case NO_RESPONSE -> {
                responded = false;
                paidAmount = BigDecimal.ZERO;
            }

            case CASH_FLOW_ISSUE -> {
                responded = true;

                double percentage =
                        0.05 + (random.nextDouble() * 0.20);

                paidAmount = invoiceAmount
                        .multiply(BigDecimal.valueOf(percentage))
                        .setScale(
                                MONEY_SCALE,
                                RoundingMode.HALF_UP
                        );
            }

            case DISPUTE -> {
                responded = true;
                paidAmount = BigDecimal.ZERO;
            }

            case PROMISE -> {
                responded = true;
                promisedPayment = true;
                paidAmount = BigDecimal.ZERO;
            }

            case BROKEN_PROMISE -> {
                responded = true;
                promisedPayment = true;
                promiseBroken = true;
                paidAmount = BigDecimal.ZERO;
            }

            case ESCALATION -> {
                responded = true;
                escalated = true;
                paidAmount = BigDecimal.ZERO;
            }

            case FAILURE -> {
                failed = true;
                responded = false;
                paidAmount = BigDecimal.ZERO;
            }
        }

        BigDecimal normalizedInvoiceAmount =
                invoiceAmount.setScale(
                        MONEY_SCALE,
                        RoundingMode.HALF_UP
                );

        BigDecimal outstandingAmount =
                normalizedInvoiceAmount
                        .subtract(paidAmount)
                        .max(BigDecimal.ZERO)
                        .setScale(
                                MONEY_SCALE,
                                RoundingMode.HALF_UP
                        );

        return new SimulationResult(
                seed,
                scenario,
                customerReference,
                invoiceReference,
                normalizedInvoiceAmount,
                paidAmount,
                outstandingAmount,
                responded,
                promisedPayment,
                promiseBroken,
                escalated,
                failed,
                clock.now()
        );
    }
}