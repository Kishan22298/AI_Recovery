package com.recovery.ReceivablesGuard.observation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Immutable observation/context produced by SignalAssembler.
 *
 * This is an observation only.
 *
 * It does NOT:
 * - diagnose the customer
 * - calculate propensity
 * - calculate expected value
 * - choose an action
 * - authorize an action
 * - execute an action
 */
public record InvoiceContext(

        String invoiceReference,

        String customerReference,

        BigDecimal outstandingAmount,

        LocalDate invoiceDate,

        LocalDate dueDate,

        long invoiceAgeDays,

        long daysOverdue,

        int historicalInvoiceCount,

        int historicalPaidInvoiceCount,

        int historicalLatePaymentCount,

        BigDecimal historicalPaidAmount,

        int paymentCount,

        BigDecimal totalPaidAmount,

        int communicationCount,

        int successfulCommunicationCount,

        int promiseCount,

        int activePromiseCount,

        int brokenPromiseCount,

        int disputeCount,

        int activeDisputeCount,

        int previousInterventionCount,

        int successfulInterventionCount,

        int responseCount,

        int contactCount,

        double paymentHistoryRate,

        double responsivenessRate,

        double interventionSuccessRate,

        double contactResponseRate,

        boolean hasPaymentHistory,

        boolean hasCommunicationHistory,

        boolean hasPromiseHistory,

        boolean hasDisputeHistory,

        boolean hasInterventionHistory,

        boolean doNotContact,

        int contactFrequency,

        LocalDateTime lastContactAt

) {

    public boolean isOverdue() {
        return daysOverdue > 0;
    }

    public boolean hasOutstandingAmount() {
        return outstandingAmount != null
                && outstandingAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    // Accessor methods for PropensityScorer compatibility
    public Integer successfulPayments() {
        return historicalPaidInvoiceCount;
    }

    public Integer failedPayments() {
        return historicalLatePaymentCount;
    }

    public Integer responsiveness() {
        return (int) Math.round(responsivenessRate * 100);
    }

    public boolean hasPromise() {
        return promiseCount > 0;
    }

    public boolean hasBrokenPromise() {
        return brokenPromiseCount > 0;
    }

    public boolean hasDispute() {
        return disputeCount > 0;
    }

    public long invoiceAgeDays() {
        return invoiceAgeDays;
    }

    // Accessor methods for PolicyEngine compatibility
    public boolean doNotContact() {
        return doNotContact;
    }

    public int contactFrequency() {
        return contactFrequency;
    }

    public LocalDateTime lastContactAt() {
        return lastContactAt;
    }

    /**
     * Creates a minimal InvoiceContext for testing purposes.
     * Uses sensible defaults for fields not relevant to propensity scoring.
     */
    public static InvoiceContext forTesting(
            BigDecimal outstandingAmount,
            long invoiceAgeDays,
            int historicalInvoiceCount,
            int historicalLatePaymentCount,
            int responsiveness,
            int promiseCount,
            int brokenPromiseCount,
            boolean hasPaymentHistory,
            boolean hasPromiseHistory,
            boolean hasDisputeHistory
    ) {
        LocalDate today = LocalDate.now();
        return new InvoiceContext(
                "INV-TEST",           // invoiceReference
                "CUST-TEST",          // customerReference
                outstandingAmount,    // outstandingAmount
                today.minusDays(invoiceAgeDays), // invoiceDate
                today,                // dueDate
                invoiceAgeDays,       // invoiceAgeDays
                Math.max(0, invoiceAgeDays - 30), // daysOverdue
                historicalInvoiceCount, // historicalInvoiceCount
                historicalInvoiceCount - historicalLatePaymentCount, // historicalPaidInvoiceCount
                historicalLatePaymentCount, // historicalLatePaymentCount
                BigDecimal.ZERO,      // historicalPaidAmount
                0,                    // paymentCount
                BigDecimal.ZERO,      // totalPaidAmount
                0,                    // communicationCount
                0,                    // successfulCommunicationCount
                promiseCount,         // promiseCount
                promiseCount,         // activePromiseCount
                brokenPromiseCount,   // brokenPromiseCount
                hasDisputeHistory ? 1 : 0, // disputeCount
                hasDisputeHistory ? 1 : 0, // activeDisputeCount
                0,                    // previousInterventionCount
                0,                    // successfulInterventionCount
                0,                    // responseCount
                0,                    // contactCount
                hasPaymentHistory ? 1.0 : 0.0, // paymentHistoryRate
                responsiveness / 100.0, // responsivenessRate
                0.0,                  // interventionSuccessRate
                0.0,                  // contactResponseRate
                hasPaymentHistory,    // hasPaymentHistory
                false,                // hasCommunicationHistory
                hasPromiseHistory,    // hasPromiseHistory
                hasDisputeHistory,    // hasDisputeHistory
                false,                // hasInterventionHistory
                false,                // doNotContact
                0,                    // contactFrequency
                null                  // lastContactAt
        );
    }
}