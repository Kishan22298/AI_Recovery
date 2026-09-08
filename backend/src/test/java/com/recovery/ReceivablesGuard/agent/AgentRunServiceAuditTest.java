package com.recovery.ReceivablesGuard.agent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.recovery.ReceivablesGuard.audit.AuditEventType;
import com.recovery.ReceivablesGuard.audit.AuditService;
import com.recovery.ReceivablesGuard.decision.DecisionEngine;
import com.recovery.ReceivablesGuard.decision.DecisionResult;
import com.recovery.ReceivablesGuard.decision.InterventionStrategy;
import com.recovery.ReceivablesGuard.decision.RankedStrategy;
import com.recovery.ReceivablesGuard.diagnosis.DiagnosisResult;
import com.recovery.ReceivablesGuard.diagnosis.DiagnosisService;
import com.recovery.ReceivablesGuard.domain.AgentRound;
import com.recovery.ReceivablesGuard.domain.AgentRun;
import com.recovery.ReceivablesGuard.domain.Customer;
import com.recovery.ReceivablesGuard.domain.Invoice;
import com.recovery.ReceivablesGuard.event.AgentEventPublisher;
import com.recovery.ReceivablesGuard.execution.ExecutionRequest;
import com.recovery.ReceivablesGuard.execution.ExecutionResult;
import com.recovery.ReceivablesGuard.observation.InvoiceContext;
import com.recovery.ReceivablesGuard.observation.ObservationService;
import com.recovery.ReceivablesGuard.outcome.OutcomeEvaluator;
import com.recovery.ReceivablesGuard.outcome.OutcomeResult;
import com.recovery.ReceivablesGuard.outcome.OutcomeType;
import com.recovery.ReceivablesGuard.policy.PolicyEngine;
import com.recovery.ReceivablesGuard.policy.PolicyResult;
import com.recovery.ReceivablesGuard.repository.AgentRoundRepository;
import com.recovery.ReceivablesGuard.repository.AgentRunRepository;
import com.recovery.ReceivablesGuard.repository.InterventionOutcomeRepository;
import com.recovery.ReceivablesGuard.repository.InvoiceRepository;
import com.recovery.ReceivablesGuard.state.StateUpdateResult;
import com.recovery.ReceivablesGuard.state.StateUpdateService;

class AgentRunServiceAuditTest {

    private InvoiceRepository invoiceRepository;
    private AgentRunRepository agentRunRepository;
    private AgentRoundRepository agentRoundRepository;

    private ObservationService observationService;
    private DiagnosisService diagnosisService;
    private DecisionEngine decisionEngine;
    private PolicyEngine policyEngine;

    private com.recovery.ReceivablesGuard.execution
            .SimulationExecutionService simulationExecutionService;

    private OutcomeEvaluator outcomeEvaluator;
    private StateUpdateService stateUpdateService;
    private InterventionOutcomeRepository interventionOutcomeRepository;
    private AuditService auditService;

    private AgentRunService agentRunService;

    @BeforeEach
    void setUp() {

        invoiceRepository =
                mock(InvoiceRepository.class);

        agentRunRepository =
                mock(AgentRunRepository.class);

        agentRoundRepository =
                mock(AgentRoundRepository.class);

        observationService =
                mock(ObservationService.class);

        diagnosisService =
                mock(DiagnosisService.class);

        decisionEngine =
                mock(DecisionEngine.class);

        policyEngine =
                mock(PolicyEngine.class);

        simulationExecutionService =
                mock(
                        com.recovery.ReceivablesGuard.execution
                                .SimulationExecutionService.class
                );

        outcomeEvaluator =
                mock(OutcomeEvaluator.class);

        stateUpdateService =
                mock(StateUpdateService.class);

        interventionOutcomeRepository =
                mock(InterventionOutcomeRepository.class);

        auditService =
                mock(AuditService.class);

        agentRunService =
                new AgentRunService(
                        invoiceRepository,
                        agentRunRepository,
                        agentRoundRepository,
                        observationService,
                        diagnosisService,
                        decisionEngine,
                        policyEngine,
                        simulationExecutionService,
                        outcomeEvaluator,
                        stateUpdateService,
                        interventionOutcomeRepository,
                        auditService,
                        org.mockito.Mockito.mock(AgentEventPublisher.class)
                );
    }

    @Test
    void shouldEmitAllThirteenAuditEventsInLifecycleOrder()
            throws Exception {

        /*
         * ============================================================
         * CUSTOMER
         * ============================================================
         */

        Customer customer =
                mock(Customer.class);

        when(customer.getExternalRef())
                .thenReturn("CUST-001");

        when(customer.isDoNotContact())
                .thenReturn(false);

        /*
         * ============================================================
         * INVOICE
         * ============================================================
         */

        Invoice invoice =
                mock(Invoice.class);

        when(invoice.getId())
                .thenReturn(100L);

        when(invoice.getExternalRef())
                .thenReturn("INV-001");

        when(invoice.getCustomer())
                .thenReturn(customer);

        when(invoice.getOutstandingAmount())
                .thenReturn(
                        new BigDecimal("10000.00")
                );

        when(invoice.getIssueDate())
                .thenReturn(
                        LocalDate.now().minusDays(45)
                );

        when(invoice.getDueDate())
                .thenReturn(
                        LocalDate.now().minusDays(15)
                );

        /*
         * We don't need a real InvoiceStatus here.
         *
         * AgentRunService only calls .name().
         */

        com.recovery.ReceivablesGuard.domain.InvoiceStatus
                invoiceStatus =
                mock(
                        com.recovery.ReceivablesGuard.domain.InvoiceStatus.class
                );

        when(invoiceStatus.name())
                .thenReturn("OPEN");

        when(invoice.getStatus())
                .thenReturn(invoiceStatus);

        when(invoiceRepository.findByExternalRef("INV-001"))
                .thenReturn(
                        Optional.of(invoice)
                );

        /*
         * ============================================================
         * AGENT RUN
         * ============================================================
         */

        when(agentRunRepository.save(any(AgentRun.class)))
                .thenAnswer(invocation -> {

                    AgentRun run =
                            invocation.getArgument(0);

                    setId(
                            run,
                            1L
                    );

                    return run;
                });

        /*
         * ============================================================
         * AGENT ROUND
         * ============================================================
         */

        when(agentRoundRepository.save(any(AgentRound.class)))
                .thenAnswer(invocation -> {

                    AgentRound round =
                            invocation.getArgument(0);

                    setId(
                            round,
                            10L
                    );

                    return round;
                });

        /*
         * ============================================================
         * OBSERVATION
         * ============================================================
         */

        InvoiceContext context =
                mock(InvoiceContext.class);

        when(context.invoiceReference())
                .thenReturn("INV-001");

        when(context.customerReference())
                .thenReturn("CUST-001");

        when(context.outstandingAmount())
                .thenReturn(
                        new BigDecimal("10000.00")
                );

        /*
         * These types are matched to the actual InvoiceContext API.
         */

        when(context.invoiceAgeDays())
                .thenReturn(45L);

        when(context.daysOverdue())
                .thenReturn(15L);

        when(context.paymentHistoryRate())
                .thenReturn(0.50);

        when(context.responsivenessRate())
                .thenReturn(0.50);

        when(context.historicalInvoiceCount())
        .thenReturn(4);

when(context.historicalPaidInvoiceCount())
        .thenReturn(2);

when(context.historicalLatePaymentCount())
        .thenReturn(1);

when(context.promiseCount())
        .thenReturn(1);

when(context.brokenPromiseCount())
        .thenReturn(0);

when(context.disputeCount())
        .thenReturn(0);

when(context.previousInterventionCount())
        .thenReturn(1);

when(context.successfulInterventionCount())
        .thenReturn(1);
        when(context.doNotContact())
                .thenReturn(false);

        when(observationService.observe(any()))
                .thenReturn(context);

        /*
         * ============================================================
         * DIAGNOSIS
         * ============================================================
         */

        DiagnosisResult diagnosis =
                mock(DiagnosisResult.class);

        when(diagnosis.category())
                .thenReturn(
                        com.recovery.ReceivablesGuard.diagnosis
                                .DiagnosisCategory
                                .values()[0]
                );

        when(diagnosis.evidence())
                .thenReturn(
                        List.of()
                );

        when(diagnosisService.diagnose(any()))
                .thenReturn(diagnosis);

        /*
         * ============================================================
         * DECISION
         * ============================================================
         */

        RankedStrategy rankedStrategy =
                new RankedStrategy(
                        InterventionStrategy.EMAIL_REMINDER,
                        new BigDecimal("0.80"),
                        new BigDecimal("10000.00"),
                        new BigDecimal("10.00"),
                        new BigDecimal("7990.00"),
                        1
                );

        DecisionResult decision =
                new DecisionResult(
                        rankedStrategy,
                        List.of(rankedStrategy)
                );

        when(decisionEngine.decide(context))
                .thenReturn(decision);

        /*
         * ============================================================
         * POLICY
         * ============================================================
         */

        PolicyResult policy =
                PolicyResult.allowed(
                        InterventionStrategy
                                .EMAIL_REMINDER
                                .name()
                );

        when(
                policyEngine.authorizeBestAvailable(
                        any(),
                        eq(context),
                        any()
                )
        ).thenReturn(policy);

        /*
         * ============================================================
         * EXECUTION
         * ============================================================
         *
         * The actual ExecutionResult API uses:
         *
         * ExecutionResult.simulated(ExecutionRequest)
         */

        when(simulationExecutionService.execute(any(
                ExecutionRequest.class
        ))).thenAnswer(invocation -> {

            ExecutionRequest request =
                    invocation.getArgument(0);

            return ExecutionResult.simulated(
                    request
            );
        });

        /*
         * ============================================================
         * OUTCOME
         * ============================================================
         */

        OutcomeResult outcome =
                new OutcomeResult(
                        OutcomeType.NO_RESPONSE,
                        new BigDecimal("10000.00"),
                        BigDecimal.ZERO,
                        new BigDecimal("10000.00"),
                        BigDecimal.ZERO,
                        false,
                        true
                );

        when(
                outcomeEvaluator.evaluate(
                        eq(OutcomeType.NO_RESPONSE),
                        eq(new BigDecimal("10000.00")),
                        eq(BigDecimal.ZERO)
                )
        ).thenReturn(outcome);

        /*
         * ============================================================
         * STATE UPDATE
         * ============================================================
         */

        StateUpdateResult stateResult =
                new StateUpdateResult(
                        100L,
                        new BigDecimal("10000.00"),
                        new BigDecimal("10000.00"),
                        "OPEN",
                        "OPEN",
                        false
                );

        when(
                stateUpdateService.applyPayment(
                        eq(100L),
                        eq(new BigDecimal("10000.00")),
                        eq(BigDecimal.ZERO),
                        eq("OPEN")
                )
        ).thenReturn(stateResult);

        /*
         * ============================================================
         * EXECUTE AGENT RUN
         * ============================================================
         */

        AgentRunResult result =
                agentRunService.run("INV-001");

        assertThat(result)
                .isNotNull();

        /*
         * ============================================================
         * CAPTURE AUDIT EVENTS
         * ============================================================
         */

        ArgumentCaptor<AuditEventType>
                eventTypeCaptor =
                ArgumentCaptor.forClass(
                        AuditEventType.class
                );

        verify(auditService, times(13))
                .append(
                        eq(1L),
                        eq(10L),
                        eventTypeCaptor.capture(),
                        eq("AGENT"),
                        anyString()
                );

        List<AuditEventType> actualTypes =
                eventTypeCaptor.getAllValues();

        /*
         * ============================================================
         * EXPECTED 13-STAGE LIFECYCLE
         * ============================================================
         */

        List<AuditEventType> expectedTypes =
                List.of(
                        AuditEventType.OBSERVED_CONTEXT,
                        AuditEventType.DIAGNOSIS,
                        AuditEventType.EVIDENCE,
                        AuditEventType.CANDIDATE_STRATEGIES,
                        AuditEventType.PROPENSITY,
                        AuditEventType.EXPECTED_VALUE,
                        AuditEventType.RANKING,
                        AuditEventType.POLICY,
                        AuditEventType.BLOCKED_ACTIONS,
                        AuditEventType.FINAL_DECISION,
                        AuditEventType.EXECUTION,
                        AuditEventType.OUTCOME,
                        AuditEventType.STATE_UPDATE
                );

        assertThat(actualTypes)
                .containsExactlyElementsOf(
                        expectedTypes
                );
    }

    /*
     * ================================================================
     * SET ENTITY ID FOR MOCKED PERSISTENCE
     * ================================================================
     */

    private static void setId(
            Object entity,
            Long id) {

        try {

            var field =
                    entity.getClass()
                            .getDeclaredField("id");

            field.setAccessible(true);

            field.set(
                    entity,
                    id
            );

        } catch (Exception exception) {

            throw new RuntimeException(
                    "Unable to set test entity ID",
                    exception
            );
        }
    }
}