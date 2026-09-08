package com.recovery.ReceivablesGuard.agent;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InOrder;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.recovery.ReceivablesGuard.domain.AgentRun;
import com.recovery.ReceivablesGuard.domain.AgentRunStatus;
import com.recovery.ReceivablesGuard.domain.Customer;
import com.recovery.ReceivablesGuard.domain.Invoice;
import com.recovery.ReceivablesGuard.domain.InvoiceStatus;
import com.recovery.ReceivablesGuard.event.AgentEventPublisher;
import com.recovery.ReceivablesGuard.repository.AgentRunRepository;
import com.recovery.ReceivablesGuard.repository.InvoiceRepository;

@ExtendWith(MockitoExtension.class)
class AgentRunManagerTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private AgentRunRepository agentRunRepository;

    @Mock
    private AgentRunService agentRunService;

    private AgentRunManager manager;

    private Invoice invoice;

    @BeforeEach
    void setUp() {

        manager = new AgentRunManager(
        invoiceRepository,
        agentRunRepository,
        agentRunService,
        org.mockito.Mockito.mock(AgentEventPublisher.class)
);

        Customer customer = mock(Customer.class);

        invoice = new Invoice(
                "INV-001",
                customer,
                new BigDecimal("10000.00"),
                "INR",
                LocalDate.now().minusDays(30),
                LocalDate.now().minusDays(10)
        );

        invoice.setStatus(InvoiceStatus.OPEN);

        setInvoiceId(invoice, 100L);
    }

    // ------------------------------------------------------------
    // 1. ROUNDS OCCUR IN ORDER
    // ------------------------------------------------------------

    @Test
    void roundsShouldOccurInOrder() {

        when(invoiceRepository
                .findByExternalRef("INV-001"))
                .thenReturn(java.util.Optional.of(invoice));

        when(invoiceRepository
                .findById(100L))
                .thenReturn(java.util.Optional.of(invoice));

        AgentRun savedRun =
                new AgentRun(invoice, 3);

        setAgentRunId(savedRun, 1L);

        when(agentRunRepository.save(any(AgentRun.class)))
                .thenReturn(savedRun);

        AgentRunResult round1 =
                mock(AgentRunResult.class);

        AgentRunResult round2 =
                mock(AgentRunResult.class);

        AgentRunResult round3 =
                mock(AgentRunResult.class);

        when(round1.isBlocked()).thenReturn(false);
        when(round2.isBlocked()).thenReturn(false);
        when(round3.isBlocked()).thenReturn(false);

        when(agentRunService.runRound(
                any(AgentRun.class),
                any(Invoice.class),
                eq(1)
        )).thenReturn(round1);

        when(agentRunService.runRound(
                any(AgentRun.class),
                any(Invoice.class),
                eq(2)
        )).thenReturn(round2);

        when(agentRunService.runRound(
                any(AgentRun.class),
                any(Invoice.class),
                eq(3)
        )).thenReturn(round3);

        manager.run("INV-001", 3);

        InOrder inOrder =
                inOrder(agentRunService);

        inOrder.verify(agentRunService)
                .runRound(
                        savedRun,
                        invoice,
                        1
                );

        inOrder.verify(agentRunService)
                .runRound(
                        savedRun,
                        invoice,
                        2
                );

        inOrder.verify(agentRunService)
                .runRound(
                        savedRun,
                        invoice,
                        3
                );

        verify(agentRunService, times(3))
                .runRound(
                        any(AgentRun.class),
                        any(Invoice.class),
                        anyInt()
                );
    }

    // ------------------------------------------------------------
    // 2. STATE CHANGES BETWEEN ROUNDS
    // ------------------------------------------------------------

    @Test
    void stateChangesBetweenRoundsShouldBeVisibleToManager() {

        when(invoiceRepository
                .findByExternalRef("INV-001"))
                .thenReturn(java.util.Optional.of(invoice));

        when(invoiceRepository
                .findById(100L))
                .thenReturn(java.util.Optional.of(invoice));

        AgentRun savedRun =
                new AgentRun(invoice, 2);

        setAgentRunId(savedRun, 1L);

        when(agentRunRepository.save(any(AgentRun.class)))
                .thenReturn(savedRun);

        AgentRunResult round1 =
                mock(AgentRunResult.class);

        AgentRunResult round2 =
                mock(AgentRunResult.class);

        when(round1.isBlocked())
                .thenReturn(false);

        when(round2.isBlocked())
                .thenReturn(false);

        when(agentRunService.runRound(
                savedRun,
                invoice,
                1
        )).thenAnswer(invocation -> {

            /*
             * Simulate the state update produced by
             * Round 1.
             */
            invoice.setOutstandingAmount(
                    new BigDecimal("7500.00")
            );

            return round1;
        });

        when(agentRunService.runRound(
                savedRun,
                invoice,
                2
        )).thenReturn(round2);

        manager.run("INV-001", 2);

        ArgumentCaptor<Invoice> invoiceCaptor =
                ArgumentCaptor.forClass(Invoice.class);

        verify(agentRunService, times(2))
                .runRound(
                        eq(savedRun),
                        invoiceCaptor.capture(),
                        anyInt()
                );

        assertThat(
                invoiceCaptor.getAllValues()
                        .get(0)
                        .getOutstandingAmount()
        ).isEqualByComparingTo(
                new BigDecimal("7500.00")
        );

        assertThat(
                invoice.getOutstandingAmount()
        ).isEqualByComparingTo(
                new BigDecimal("7500.00")
        );
    }

    // ------------------------------------------------------------
    // 3. ROUND 2 SEES UPDATED STATE
    // ------------------------------------------------------------

    @Test
    void round2ShouldReceiveUpdatedInvoiceState() {

        when(invoiceRepository
                .findByExternalRef("INV-001"))
                .thenReturn(java.util.Optional.of(invoice));

        when(invoiceRepository
                .findById(100L))
                .thenReturn(java.util.Optional.of(invoice));

        AgentRun savedRun =
                new AgentRun(invoice, 2);

        setAgentRunId(savedRun, 1L);

        when(agentRunRepository.save(any(AgentRun.class)))
                .thenReturn(savedRun);

        AgentRunResult round1 =
                mock(AgentRunResult.class);

        AgentRunResult round2 =
                mock(AgentRunResult.class);

        when(round1.isBlocked())
                .thenReturn(false);

        when(round2.isBlocked())
                .thenReturn(false);

        when(agentRunService.runRound(
                savedRun,
                invoice,
                1
        )).thenAnswer(invocation -> {

            /*
             * Round 1 recovered ₹2,500.
             */
            invoice.setOutstandingAmount(
                    new BigDecimal("7500.00")
            );

            return round1;
        });

        when(agentRunService.runRound(
                savedRun,
                invoice,
                2
        )).thenAnswer(invocation -> {

            /*
             * Assert that Round 2 receives the
             * state created by Round 1.
             */
            Invoice round2Invoice =
                    invocation.getArgument(
                            1,
                            Invoice.class
                    );

            assertThat(
                    round2Invoice.getOutstandingAmount()
            ).isEqualByComparingTo(
                    new BigDecimal("7500.00")
            );

            return round2;
        });

        manager.run("INV-001", 2);

        verify(agentRunService)
                .runRound(
                        savedRun,
                        invoice,
                        2
                );
    }

    // ------------------------------------------------------------
    // 4. DECISIONS CAN CHANGE
    // ------------------------------------------------------------

    @Test
    void decisionsCanChangeBetweenRounds() {

        when(invoiceRepository
                .findByExternalRef("INV-001"))
                .thenReturn(java.util.Optional.of(invoice));

        when(invoiceRepository
                .findById(100L))
                .thenReturn(java.util.Optional.of(invoice));

        AgentRun savedRun =
                new AgentRun(invoice, 2);

        setAgentRunId(savedRun, 1L);

        when(agentRunRepository.save(any(AgentRun.class)))
                .thenReturn(savedRun);

        AgentRunResult round1 =
                mock(AgentRunResult.class);

        AgentRunResult round2 =
                mock(AgentRunResult.class);

        when(round1.isBlocked())
                .thenReturn(false);

        when(round2.isBlocked())
                .thenReturn(false);

        when(agentRunService.runRound(
                savedRun,
                invoice,
                1
        )).thenAnswer(invocation -> {

            invoice.setOutstandingAmount(
                    new BigDecimal("7500.00")
            );

            return round1;
        });

        when(agentRunService.runRound(
                savedRun,
                invoice,
                2
        )).thenReturn(round2);

        manager.run("INV-001", 2);

        /*
         * The manager itself does not select strategies.
         *
         * AgentRunService owns the decision for each round.
         *
         * Therefore verify that two independent round
         * executions were requested.
         */
        InOrder inOrder =
                inOrder(agentRunService);

        inOrder.verify(agentRunService)
                .runRound(
                        savedRun,
                        invoice,
                        1
                );

        inOrder.verify(agentRunService)
                .runRound(
                        savedRun,
                        invoice,
                        2
                );
    }

    // ------------------------------------------------------------
    // 5. MAXIMUM ROUNDS ENFORCED
    // ------------------------------------------------------------

    @Test
    void maximumRoundsShouldBeEnforced() {

        when(invoiceRepository
                .findByExternalRef("INV-001"))
                .thenReturn(java.util.Optional.of(invoice));

        when(invoiceRepository
                .findById(100L))
                .thenReturn(java.util.Optional.of(invoice));

        AgentRun savedRun =
                new AgentRun(invoice, 2);

        setAgentRunId(savedRun, 1L);

        when(agentRunRepository.save(any(AgentRun.class)))
                .thenReturn(savedRun);

        AgentRunResult round1 =
                mock(AgentRunResult.class);

        AgentRunResult round2 =
                mock(AgentRunResult.class);

        when(round1.isBlocked())
                .thenReturn(false);

        when(round2.isBlocked())
                .thenReturn(false);

        when(agentRunService.runRound(
                savedRun,
                invoice,
                1
        )).thenReturn(round1);

        when(agentRunService.runRound(
                savedRun,
                invoice,
                2
        )).thenReturn(round2);

        manager.run("INV-001", 2);

        verify(agentRunService, times(2))
                .runRound(
                        eq(savedRun),
                        eq(invoice),
                        anyInt()
                );

        verify(agentRunService, never())
                .runRound(
                        eq(savedRun),
                        eq(invoice),
                        eq(3)
                );
    }

    // ------------------------------------------------------------
    // 6. TERMINATION WHEN INVOICE IS FULLY PAID
    // ------------------------------------------------------------

    @Test
    void runShouldTerminateWhenInvoiceIsFullyPaid() {

        when(invoiceRepository
                .findByExternalRef("INV-001"))
                .thenReturn(java.util.Optional.of(invoice));

        when(invoiceRepository
                .findById(100L))
                .thenReturn(java.util.Optional.of(invoice));

        AgentRun savedRun =
                new AgentRun(invoice, 3);

        setAgentRunId(savedRun, 1L);

        when(agentRunRepository.save(any(AgentRun.class)))
                .thenReturn(savedRun);

        AgentRunResult round1 =
                mock(AgentRunResult.class);

        when(round1.isBlocked())
                .thenReturn(false);

        when(agentRunService.runRound(
                savedRun,
                invoice,
                1
        )).thenAnswer(invocation -> {

            /*
             * Full payment occurred.
             */
            invoice.setOutstandingAmount(
                    BigDecimal.ZERO
            );

            invoice.setStatus(
                    InvoiceStatus.PAID
            );

            return round1;
        });

        manager.run("INV-001", 3);

        verify(agentRunService, times(1))
                .runRound(
                        savedRun,
                        invoice,
                        1
                );

        verify(agentRunService, never())
                .runRound(
                        any(AgentRun.class),
                        any(Invoice.class),
                        eq(2)
                );

        verify(agentRunService, never())
                .runRound(
                        any(AgentRun.class),
                        any(Invoice.class),
                        eq(3)
                );
    }

    // ------------------------------------------------------------
    // 7. TERMINATION WHEN POLICY BLOCKS
    // ------------------------------------------------------------

    @Test
    void runShouldTerminateWhenRoundIsBlocked() {

        when(invoiceRepository
                .findByExternalRef("INV-001"))
                .thenReturn(java.util.Optional.of(invoice));

        when(invoiceRepository
                .findById(100L))
                .thenReturn(java.util.Optional.of(invoice));

        AgentRun savedRun =
                new AgentRun(invoice, 3);

        setAgentRunId(savedRun, 1L);

        when(agentRunRepository.save(any(AgentRun.class)))
                .thenReturn(savedRun);

        AgentRunResult blockedRound =
                mock(AgentRunResult.class);

        when(blockedRound.isBlocked())
                .thenReturn(true);

        when(agentRunService.runRound(
                savedRun,
                invoice,
                1
        )).thenReturn(blockedRound);

        manager.run("INV-001", 3);

        verify(agentRunService, times(1))
                .runRound(
                        savedRun,
                        invoice,
                        1
                );

        verify(agentRunService, never())
                .runRound(
                        any(AgentRun.class),
                        any(Invoice.class),
                        eq(2)
                );
    }

    // ------------------------------------------------------------
    // 8. INVALID MAX ROUNDS
    // ------------------------------------------------------------

    @Test
    void maxRoundsMustBePositive() {

        assertThatThrownBy(() ->
                manager.run(
                        "INV-001",
                        0
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "maxRounds must be greater than zero"
                );

        verifyNoInteractions(
                invoiceRepository,
                agentRunRepository,
                agentRunService
        );
    }

    @Test
    void negativeMaxRoundsShouldBeRejected() {

        assertThatThrownBy(() ->
                manager.run(
                        "INV-001",
                        -1
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "maxRounds must be greater than zero"
                );

        verifyNoInteractions(
                invoiceRepository,
                agentRunRepository,
                agentRunService
        );
    }

    // ------------------------------------------------------------
    // 9. INVALID INVOICE REFERENCE
    // ------------------------------------------------------------

    @Test
    void blankInvoiceReferenceShouldBeRejected() {

        assertThatThrownBy(() ->
                manager.run(
                        "   ",
                        3
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Invoice reference must not be blank"
                );

        verifyNoInteractions(
                invoiceRepository,
                agentRunRepository,
                agentRunService
        );
    }

    // ------------------------------------------------------------
    // 10. RUN COMPLETES AFTER MAX ROUNDS
    // ------------------------------------------------------------

    @Test
    void runShouldCompleteAfterMaximumRounds() {

        when(invoiceRepository
                .findByExternalRef("INV-001"))
                .thenReturn(java.util.Optional.of(invoice));

        when(invoiceRepository
                .findById(100L))
                .thenReturn(java.util.Optional.of(invoice));

        AgentRun savedRun =
                new AgentRun(invoice, 2);

        setAgentRunId(savedRun, 1L);

        when(agentRunRepository.save(any(AgentRun.class)))
                .thenReturn(savedRun);

        AgentRunResult round1 =
                mock(AgentRunResult.class);

        AgentRunResult round2 =
                mock(AgentRunResult.class);

        when(round1.isBlocked())
                .thenReturn(false);

        when(round2.isBlocked())
                .thenReturn(false);

        when(agentRunService.runRound(
                savedRun,
                invoice,
                1
        )).thenReturn(round1);

        when(agentRunService.runRound(
                savedRun,
                invoice,
                2
        )).thenReturn(round2);

        manager.run("INV-001", 2);

        assertThat(savedRun.getStatus())
                .isEqualTo(
                        AgentRunStatus.COMPLETED
                );

        assertThat(savedRun.getCompletedAt())
                .isNotNull();

        verify(agentRunRepository, atLeastOnce())
                .save(savedRun);
    }

    // ------------------------------------------------------------
    // HELPERS
    // ------------------------------------------------------------

    private static void setInvoiceId(
            Invoice invoice,
            Long id) {

        try {

            java.lang.reflect.Field field =
                    Invoice.class.getDeclaredField("id");

            field.setAccessible(true);
            field.set(invoice, id);

        } catch (ReflectiveOperationException exception) {

            throw new AssertionError(
                    "Unable to set invoice ID",
                    exception
            );
        }
    }

    private static void setAgentRunId(
            AgentRun agentRun,
            Long id) {

        try {

            java.lang.reflect.Field field =
                    AgentRun.class.getDeclaredField("id");

            field.setAccessible(true);
            field.set(agentRun, id);

        } catch (ReflectiveOperationException exception) {

            throw new AssertionError(
                    "Unable to set agent run ID",
                    exception
            );
        }
    }
}