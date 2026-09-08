package com.recovery.ReceivablesGuard.outcome;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

@Component
public class OutcomeEvaluator {

    private static final int MONEY_SCALE = 2;
    private static final int RATE_SCALE = 6;

    public OutcomeResult evaluate(
            OutcomeType outcomeType,
            BigDecimal invoiceAmount,
            BigDecimal paidAmount
    ) {

        validateInvoiceAmount(invoiceAmount);
        validatePaidAmount(paidAmount);

        BigDecimal normalizedInvoice =
                invoiceAmount.setScale(
                        MONEY_SCALE,
                        RoundingMode.HALF_UP
                );

        BigDecimal normalizedPaid =
                paidAmount.setScale(
                        MONEY_SCALE,
                        RoundingMode.HALF_UP
                );

        if (normalizedPaid.compareTo(normalizedInvoice) > 0) {
            normalizedPaid = normalizedInvoice;
        }

        BigDecimal remainingAmount =
                normalizedInvoice
                        .subtract(normalizedPaid)
                        .max(BigDecimal.ZERO)
                        .setScale(
                                MONEY_SCALE,
                                RoundingMode.HALF_UP
                        );

        BigDecimal recoveryRate =
                calculateRecoveryRate(
                        normalizedPaid,
                        normalizedInvoice
                );

        boolean successful;
        boolean requiresFollowUp;

        switch (outcomeType) {

            case PAYMENT -> {
                normalizedPaid = normalizedInvoice;

                remainingAmount = BigDecimal.ZERO
                        .setScale(
                                MONEY_SCALE,
                                RoundingMode.HALF_UP
                        );

                recoveryRate = BigDecimal.ONE
                        .setScale(
                                RATE_SCALE,
                                RoundingMode.HALF_UP
                        );

                successful = true;
                requiresFollowUp = false;
            }

            case PARTIAL_PAYMENT -> {

                if (normalizedPaid.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException(
                            "Partial payment must be greater than zero"
                    );
                }

                if (normalizedPaid.compareTo(normalizedInvoice) >= 0) {
                    throw new IllegalArgumentException(
                            "Partial payment must be less than invoice amount"
                    );
                }

                successful = true;
                requiresFollowUp = true;
            }

            case PROMISE -> {

                normalizedPaid = BigDecimal.ZERO
                        .setScale(
                                MONEY_SCALE,
                                RoundingMode.HALF_UP
                        );

                remainingAmount = normalizedInvoice;

                recoveryRate = BigDecimal.ZERO
                        .setScale(
                                RATE_SCALE,
                                RoundingMode.HALF_UP
                        );

                successful = true;
                requiresFollowUp = true;
            }

            case BROKEN_PROMISE -> {

                normalizedPaid = BigDecimal.ZERO
                        .setScale(
                                MONEY_SCALE,
                                RoundingMode.HALF_UP
                        );

                remainingAmount = normalizedInvoice;

                recoveryRate = BigDecimal.ZERO
                        .setScale(
                                RATE_SCALE,
                                RoundingMode.HALF_UP
                        );

                successful = false;
                requiresFollowUp = true;
            }

            case NO_RESPONSE -> {

                normalizedPaid = BigDecimal.ZERO
                        .setScale(
                                MONEY_SCALE,
                                RoundingMode.HALF_UP
                        );

                remainingAmount = normalizedInvoice;

                recoveryRate = BigDecimal.ZERO
                        .setScale(
                                RATE_SCALE,
                                RoundingMode.HALF_UP
                        );

                successful = false;
                requiresFollowUp = true;
            }

            case DISPUTE -> {

                normalizedPaid = BigDecimal.ZERO
                        .setScale(
                                MONEY_SCALE,
                                RoundingMode.HALF_UP
                        );

                remainingAmount = normalizedInvoice;

                recoveryRate = BigDecimal.ZERO
                        .setScale(
                                RATE_SCALE,
                                RoundingMode.HALF_UP
                        );

                successful = false;
                requiresFollowUp = true;
            }

            case EXECUTION_FAILURE -> {

                normalizedPaid = BigDecimal.ZERO
                        .setScale(
                                MONEY_SCALE,
                                RoundingMode.HALF_UP
                        );

                remainingAmount = normalizedInvoice;

                recoveryRate = BigDecimal.ZERO
                        .setScale(
                                RATE_SCALE,
                                RoundingMode.HALF_UP
                        );

                successful = false;
                requiresFollowUp = true;
            }

            default -> throw new IllegalArgumentException(
                    "Unsupported outcome type: " + outcomeType
            );
        }

        return new OutcomeResult(
                outcomeType,
                normalizedInvoice,
                normalizedPaid,
                remainingAmount,
                recoveryRate,
                successful,
                requiresFollowUp
        );
    }

    private BigDecimal calculateRecoveryRate(
            BigDecimal paidAmount,
            BigDecimal invoiceAmount
    ) {

        if (invoiceAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO
                    .setScale(
                            RATE_SCALE,
                            RoundingMode.HALF_UP
                    );
        }

        return paidAmount
                .divide(
                        invoiceAmount,
                        RATE_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private void validateInvoiceAmount(
            BigDecimal invoiceAmount
    ) {

        if (invoiceAmount == null) {
            throw new IllegalArgumentException(
                    "invoiceAmount must not be null"
            );
        }

        if (invoiceAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "invoiceAmount must be greater than zero"
            );
        }
    }

    private void validatePaidAmount(
            BigDecimal paidAmount
    ) {

        if (paidAmount == null) {
            throw new IllegalArgumentException(
                    "paidAmount must not be null"
            );
        }

        if (paidAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "paidAmount must not be negative"
            );
        }
    }
}