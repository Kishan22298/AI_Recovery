package com.recovery.ReceivablesGuard.observation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Immutable input snapshot used to construct an InvoiceContext.
 *
 * This class deliberately contains no AI logic and no decision logic.
 * It represents the currently observed state of an invoice/customer.
 *
 * Null is allowed for optional signals.
 */
public record ObservationInput(

        String invoiceReference,

        String customerReference,

        BigDecimal outstandingAmount,

        LocalDate invoiceDate,

        LocalDate dueDate,

        Integer historicalInvoiceCount,

        Integer historicalPaidInvoiceCount,

        Integer historicalLatePaymentCount,

        BigDecimal historicalPaidAmount,

        Integer paymentCount,

        BigDecimal totalPaidAmount,

        Integer communicationCount,

        Integer successfulCommunicationCount,

        Integer promiseCount,

        Integer activePromiseCount,

        Integer brokenPromiseCount,

        Integer disputeCount,

        Integer activeDisputeCount,

        Integer previousInterventionCount,

        Integer successfulInterventionCount,

        Integer responseCount,

        Integer contactCount,

        Boolean doNotContact,

        Integer contactFrequency,

        LocalDateTime lastContactAt

) {

    public ObservationInput {
        Objects.requireNonNull(invoiceReference, "invoiceReference must not be null");
        Objects.requireNonNull(customerReference, "customerReference must not be null");
    }

    public static ObservationInput minimal(
            String invoiceReference,
            String customerReference,
            BigDecimal outstandingAmount,
            LocalDate invoiceDate,
            LocalDate dueDate
    ) {
        return new ObservationInput(
                invoiceReference,
                customerReference,
                outstandingAmount,
                invoiceDate,
                dueDate,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}