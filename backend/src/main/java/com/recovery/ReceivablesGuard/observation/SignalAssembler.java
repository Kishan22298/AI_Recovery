package com.recovery.ReceivablesGuard.observation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

/**
 * Deterministically converts raw invoice/customer signals
 * into an immutable InvoiceContext.
 *
 * Important architecture rule:
 *
 * SignalAssembler observes.
 *
 * It does NOT:
 * - use Gemini
 * - diagnose root cause
 * - calculate propensity
 * - calculate expected value
 * - rank actions
 * - execute actions
 */
@Service
public class SignalAssembler {

    private static final int MONEY_SCALE = 2;
    private static final int RATE_SCALE = 6;

    /**
     * Build an observation from the supplied state.
     *
     * The same input always produces the same output.
     */
    public InvoiceContext assemble(ObservationInput input) {

        BigDecimal outstandingAmount =
                money(input.outstandingAmount());

        LocalDate invoiceDate = input.invoiceDate();
        LocalDate dueDate = input.dueDate();

        long invoiceAgeDays = calculateInvoiceAge(invoiceDate);
        long daysOverdue = calculateDaysOverdue(dueDate);

        int historicalInvoiceCount =
                nonNegative(input.historicalInvoiceCount());

        int historicalPaidInvoiceCount =
                nonNegative(input.historicalPaidInvoiceCount());

        int historicalLatePaymentCount =
                nonNegative(input.historicalLatePaymentCount());

        BigDecimal historicalPaidAmount =
                money(input.historicalPaidAmount());

        int paymentCount =
                nonNegative(input.paymentCount());

        BigDecimal totalPaidAmount =
                money(input.totalPaidAmount());

        int communicationCount =
                nonNegative(input.communicationCount());

        int successfulCommunicationCount =
                nonNegative(input.successfulCommunicationCount());

        int promiseCount =
                nonNegative(input.promiseCount());

        int activePromiseCount =
                nonNegative(input.activePromiseCount());

        int brokenPromiseCount =
                nonNegative(input.brokenPromiseCount());

        int disputeCount =
                nonNegative(input.disputeCount());

        int activeDisputeCount =
                nonNegative(input.activeDisputeCount());

        int previousInterventionCount =
                nonNegative(input.previousInterventionCount());

        int successfulInterventionCount =
                nonNegative(input.successfulInterventionCount());

        int responseCount =
                nonNegative(input.responseCount());

        int contactCount =
                nonNegative(input.contactCount());

        double paymentHistoryRate =
                ratio(
                        historicalPaidInvoiceCount,
                        historicalInvoiceCount
                );

        double responsivenessRate =
                ratio(
                        successfulCommunicationCount,
                        communicationCount
                );

        double interventionSuccessRate =
                ratio(
                        successfulInterventionCount,
                        previousInterventionCount
                );

        double contactResponseRate =
                ratio(
                        responseCount,
                        contactCount
                );

        return new InvoiceContext(
                input.invoiceReference(),
                input.customerReference(),

                outstandingAmount,

                invoiceDate,
                dueDate,

                invoiceAgeDays,
                daysOverdue,

                historicalInvoiceCount,
                historicalPaidInvoiceCount,
                historicalLatePaymentCount,
                historicalPaidAmount,

                paymentCount,
                totalPaidAmount,

                communicationCount,
                successfulCommunicationCount,

                promiseCount,
                activePromiseCount,
                brokenPromiseCount,

                disputeCount,
                activeDisputeCount,

                previousInterventionCount,
                successfulInterventionCount,

                responseCount,
                contactCount,

                paymentHistoryRate,
                responsivenessRate,
                interventionSuccessRate,
                contactResponseRate,

                historicalInvoiceCount > 0
                        || historicalPaidInvoiceCount > 0
                        || historicalLatePaymentCount > 0,

                communicationCount > 0
                        || successfulCommunicationCount > 0,

                promiseCount > 0
                        || activePromiseCount > 0
                        || brokenPromiseCount > 0,

                disputeCount > 0
                        || activeDisputeCount > 0,

                previousInterventionCount > 0
                        || successfulInterventionCount > 0,

                input.doNotContact() != null ? input.doNotContact() : false,
                input.contactFrequency() != null ? input.contactFrequency() : 0,
                input.lastContactAt()
        );
    }

    private long calculateInvoiceAge(LocalDate invoiceDate) {

        if (invoiceDate == null) {
            return 0;
        }

        long age = ChronoUnit.DAYS.between(
                invoiceDate,
                LocalDate.now()
        );

        return Math.max(age, 0);
    }

    private long calculateDaysOverdue(LocalDate dueDate) {

        if (dueDate == null) {
            return 0;
        }

        long overdue = ChronoUnit.DAYS.between(
                dueDate,
                LocalDate.now()
        );

        return Math.max(overdue, 0);
    }

    private int nonNegative(Integer value) {

        if (value == null) {
            return 0;
        }

        return Math.max(value, 0);
    }

    private BigDecimal money(BigDecimal value) {

        if (value == null) {
            return BigDecimal.ZERO.setScale(
                    MONEY_SCALE,
                    RoundingMode.HALF_UP
            );
        }

        return value.setScale(
                MONEY_SCALE,
                RoundingMode.HALF_UP
        );
    }

    private double ratio(int numerator, int denominator) {

        if (denominator <= 0) {
            return 0.0;
        }

        return BigDecimal.valueOf(numerator)
                .divide(
                        BigDecimal.valueOf(denominator),
                        RATE_SCALE,
                        RoundingMode.HALF_UP
                )
                .doubleValue();
    }
}