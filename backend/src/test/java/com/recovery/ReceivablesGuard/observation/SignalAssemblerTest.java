package com.recovery.ReceivablesGuard.observation;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class SignalAssemblerTest {

    private final SignalAssembler assembler =
            new SignalAssembler();

    private static final LocalDate TODAY =
            LocalDate.now();

    @Test
    void shouldAssembleNormalData() {

        ObservationInput input = new ObservationInput(
                "INV-001",
                "CUS-001",
                new BigDecimal("10000.00"),
                TODAY.minusDays(60),
                TODAY.minusDays(30),

                10,
                8,
                2,
                new BigDecimal("80000.00"),

                8,
                new BigDecimal("80000.00"),

                6,
                5,

                3,
                1,
                1,

                1,
                1,

                4,
                3,

                5,
                6,

                null,  // doNotContact
                null,  // contactFrequency
                null   // lastContactAt
        );

        InvoiceContext context =
                assembler.assemble(input);

        assertEquals("INV-001", context.invoiceReference());
        assertEquals("CUS-001", context.customerReference());

        assertEquals(
                new BigDecimal("10000.00"),
                context.outstandingAmount()
        );

        assertEquals(60, context.invoiceAgeDays());
        assertEquals(30, context.daysOverdue());

        assertEquals(10, context.historicalInvoiceCount());
        assertEquals(8, context.historicalPaidInvoiceCount());
        assertEquals(2, context.historicalLatePaymentCount());

        assertEquals(8, context.paymentCount());

        assertEquals(6, context.communicationCount());
        assertEquals(5, context.successfulCommunicationCount());

        assertEquals(3, context.promiseCount());
        assertEquals(1, context.activePromiseCount());
        assertEquals(1, context.brokenPromiseCount());

        assertEquals(1, context.disputeCount());
        assertEquals(1, context.activeDisputeCount());

        assertEquals(4, context.previousInterventionCount());
        assertEquals(3, context.successfulInterventionCount());

        assertEquals(5, context.responseCount());
        assertEquals(6, context.contactCount());

        assertTrue(context.hasPaymentHistory());
        assertTrue(context.hasCommunicationHistory());
        assertTrue(context.hasPromiseHistory());
        assertTrue(context.hasDisputeHistory());
        assertTrue(context.hasInterventionHistory());

        assertTrue(context.isOverdue());
        assertTrue(context.hasOutstandingAmount());
    }

    @Test
    void shouldHandleMissingOptionalData() {

        ObservationInput input =
                ObservationInput.minimal(
                        "INV-002",
                        "CUS-002",
                        new BigDecimal("5000.00"),
                        TODAY.minusDays(10),
                        TODAY.plusDays(5)
                );

        InvoiceContext context =
                assembler.assemble(input);

        assertEquals(
                new BigDecimal("5000.00"),
                context.outstandingAmount()
        );

        assertEquals(10, context.invoiceAgeDays());
        assertEquals(0, context.daysOverdue());

        assertEquals(
                0,
                context.historicalInvoiceCount()
        );

        assertEquals(
                0,
                context.paymentCount()
        );

        assertEquals(
                0,
                context.communicationCount()
        );

        assertEquals(
                0,
                context.promiseCount()
        );

        assertEquals(
                0,
                context.disputeCount()
        );

        assertEquals(
                0,
                context.previousInterventionCount()
        );

        assertEquals(
                0,
                context.responseCount()
        );

        assertEquals(
                0.0,
                context.paymentHistoryRate()
        );

        assertEquals(
                0.0,
                context.responsivenessRate()
        );

        assertEquals(
                0.0,
                context.interventionSuccessRate()
        );

        assertEquals(
                0.0,
                context.contactResponseRate()
        );
    }

    @Test
    void shouldHandleZeroOutstandingAmountBoundary() {

        ObservationInput input =
                ObservationInput.minimal(
                        "INV-003",
                        "CUS-003",
                        BigDecimal.ZERO,
                        TODAY,
                        TODAY
                );

        InvoiceContext context =
                assembler.assemble(input);

        assertEquals(
                BigDecimal.ZERO.setScale(2),
                context.outstandingAmount()
        );

        assertFalse(context.hasOutstandingAmount());
        assertFalse(context.isOverdue());
    }

    @Test
    void shouldHandleVeryLargeOutstandingAmount() {

        BigDecimal amount =
                new BigDecimal("999999999999.99");

        ObservationInput input =
                ObservationInput.minimal(
                        "INV-004",
                        "CUS-004",
                        amount,
                        TODAY.minusDays(365),
                        TODAY.minusDays(300)
                );

        InvoiceContext context =
                assembler.assemble(input);

        assertEquals(
                amount,
                context.outstandingAmount()
        );

        assertEquals(
                365,
                context.invoiceAgeDays()
        );

        assertEquals(
                300,
                context.daysOverdue()
        );

        assertTrue(context.hasOutstandingAmount());
        assertTrue(context.isOverdue());
    }

    @Test
    void shouldHandleContradictoryDataWithoutThrowing() {

        ObservationInput input = new ObservationInput(
                "INV-005",
                "CUS-005",

                new BigDecimal("1000.00"),

                TODAY.minusDays(20),
                TODAY.minusDays(10),

                // Contradictory historical state:
                // more paid invoices than total invoices.
                5,
                8,
                20,
                new BigDecimal("50000.00"),

                // More payments than historical invoices.
                20,
                new BigDecimal("100000.00"),

                // More successful communications than communications.
                2,
                10,

                // More active promises than total promises.
                1,
                5,
                10,

                // More active disputes than disputes.
                1,
                4,

                // More successful interventions than interventions.
                1,
                3,

                // More responses than contacts.
                10,
                2,

                null,  // doNotContact
                null,  // contactFrequency
                null   // lastContactAt
        );

        assertDoesNotThrow(() ->
                assembler.assemble(input)
        );

        InvoiceContext context =
                assembler.assemble(input);

        assertEquals(
                5,
                context.historicalInvoiceCount()
        );

        assertEquals(
                8,
                context.historicalPaidInvoiceCount()
        );

        assertEquals(
                10,
                context.responseCount()
        );

        assertEquals(
                2,
                context.contactCount()
        );

        /*
         * Observation does not "fix" contradictory source data.
         * It preserves the observed values.
         *
         * Diagnosis/policy layers can later decide how such
         * contradictions should affect confidence or fallback.
         */
        assertTrue(
                context.historicalPaidInvoiceCount()
                        > context.historicalInvoiceCount()
        );

        assertTrue(
                context.responseCount()
                        > context.contactCount()
        );
    }

    @Test
    void sameStateMustProduceSameObservation() {

        ObservationInput input = new ObservationInput(
                "INV-006",
                "CUS-006",
                new BigDecimal("7500.00"),
                TODAY.minusDays(45),
                TODAY.minusDays(15),

                20,
                15,
                5,
                new BigDecimal("90000.00"),

                15,
                new BigDecimal("90000.00"),

                10,
                7,

                4,
                1,
                2,

                2,
                1,

                6,
                5,

                8,
                10,

                null,  // doNotContact
                null,  // contactFrequency
                null   // lastContactAt
        );

        InvoiceContext first =
                assembler.assemble(input);

        InvoiceContext second =
                assembler.assemble(input);

        assertEquals(first, second);
    }

    @Test
    void paymentHistoryRateShouldBeDeterministic() {

        ObservationInput input = new ObservationInput(
                "INV-007",
                "CUS-007",
                new BigDecimal("1000"),
                TODAY,
                TODAY,

                10,
                7,
                3,
                new BigDecimal("7000"),

                7,
                new BigDecimal("7000"),

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

                null,  // doNotContact
                null,  // contactFrequency
                null   // lastContactAt
        );

        InvoiceContext context =
                assembler.assemble(input);

        assertEquals(
                0.7,
                context.paymentHistoryRate(),
                0.000001
        );
    }

    @Test
    void responsivenessRateShouldBeDeterministic() {

        ObservationInput input = new ObservationInput(
                "INV-008",
                "CUS-008",
                new BigDecimal("1000"),
                TODAY,
                TODAY,

                null,
                null,
                null,
                null,

                null,
                null,

                10,
                8,

                null,
                null,
                null,

                null,
                null,

                null,
                null,

                null,
                null,

                null,  // doNotContact
                null,  // contactFrequency
                null   // lastContactAt
        );

        InvoiceContext context =
                assembler.assemble(input);

        assertEquals(
                0.8,
                context.responsivenessRate(),
                0.000001
        );
    }

    @Test
    void interventionSuccessRateShouldBeDeterministic() {

        ObservationInput input = new ObservationInput(
                "INV-009",
                "CUS-009",
                new BigDecimal("1000"),
                TODAY,
                TODAY,

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

                10,
                6,

                null,
                null,

                null,  // doNotContact
                null,  // contactFrequency
                null   // lastContactAt
        );

        InvoiceContext context =
                assembler.assemble(input);

        assertEquals(
                0.6,
                context.interventionSuccessRate(),
                0.000001
        );
    }

    @Test
    void contactResponseRateShouldBeDeterministic() {

        ObservationInput input = new ObservationInput(
                "INV-010",
                "CUS-010",
                new BigDecimal("1000"),
                TODAY,
                TODAY,

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

                4,
                8,

                null,  // doNotContact
                null,  // contactFrequency
                null   // lastContactAt
        );

        InvoiceContext context =
                assembler.assemble(input);

        assertEquals(
                0.5,
                context.contactResponseRate(),
                0.000001
        );
    }

    @Test
    void nullMoneyValuesShouldBecomeZero() {

        ObservationInput input =
                ObservationInput.minimal(
                        "INV-011",
                        "CUS-011",
                        null,
                        null,
                        null
                );

        InvoiceContext context =
                assembler.assemble(input);

        assertEquals(
                BigDecimal.ZERO.setScale(2),
                context.outstandingAmount()
        );

        assertEquals(
                BigDecimal.ZERO.setScale(2),
                context.historicalPaidAmount()
        );

        assertEquals(
                BigDecimal.ZERO.setScale(2),
                context.totalPaidAmount()
        );
    }

    @Test
    void negativeCountersShouldNotBecomeNegativeSignals() {

        ObservationInput input = new ObservationInput(
                "INV-012",
                "CUS-012",
                new BigDecimal("1000"),
                TODAY,
                TODAY,

                -10,
                -8,
                -2,
                new BigDecimal("5000"),

                -7,
                new BigDecimal("5000"),

                -5,
                -4,

                -3,
                -1,
                -2,

                -2,
                -1,

                -6,
                -5,

                -8,
                -10,

                null,  // doNotContact
                null,  // contactFrequency
                null   // lastContactAt
        );

        InvoiceContext context =
                assembler.assemble(input);

        assertEquals(
                0,
                context.historicalInvoiceCount()
        );

        assertEquals(
                0,
                context.historicalPaidInvoiceCount()
        );

        assertEquals(
                0,
                context.paymentCount()
        );

        assertEquals(
                0,
                context.communicationCount()
        );

        assertEquals(
                0,
                context.promiseCount()
        );

        assertEquals(
                0,
                context.disputeCount()
        );

        assertEquals(
                0,
                context.previousInterventionCount()
        );

        assertEquals(
                0,
                context.responseCount()
        );

        assertEquals(
                0,
                context.contactCount()
        );
    }
}