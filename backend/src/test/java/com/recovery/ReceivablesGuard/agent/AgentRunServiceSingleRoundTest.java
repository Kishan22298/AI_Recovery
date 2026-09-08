package com.recovery.ReceivablesGuard.agent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.recovery.ReceivablesGuard.audit.AuditEventType;
import com.recovery.ReceivablesGuard.audit.AuditService;
import com.recovery.ReceivablesGuard.decision.DecisionEngine;
import com.recovery.ReceivablesGuard.decision.DecisionResult;
import com.recovery.ReceivablesGuard.decision.InterventionStrategy;
import com.recovery.ReceivablesGuard.decision.RankedStrategy;
import com.recovery.ReceivablesGuard.diagnosis.DiagnosisCategory;
import com.recovery.ReceivablesGuard.diagnosis.DiagnosisResult;
import com.recovery.ReceivablesGuard.diagnosis.DiagnosisService;
import com.recovery.ReceivablesGuard.domain.AgentRound;
import com.recovery.ReceivablesGuard.domain.AgentRun;
import com.recovery.ReceivablesGuard.domain.Customer;
import com.recovery.ReceivablesGuard.domain.Invoice;
import com.recovery.ReceivablesGuard.domain.InvoiceStatus;
import com.recovery.ReceivablesGuard.execution.ExecutionRequest;
import com.recovery.ReceivablesGuard.execution.ExecutionResult;
import com.recovery.ReceivablesGuard.execution.SimulationExecutionService;
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
import com.recovery.ReceivablesGuard.event.AgentEventPublisher;
import com.recovery.ReceivablesGuard.event.AgentEventType;

class AgentRunServiceSingleRoundTest {

    private InvoiceRepository invoiceRepository;
    private AgentRunRepository agentRunRepository;
    private AgentRoundRepository agentRoundRepository;

    private ObservationService observationService;
    private DiagnosisService diagnosisService;
    private DecisionEngine decisionEngine;
    private PolicyEngine policyEngine;
    private SimulationExecutionService simulationExecutionService;
    private OutcomeEvaluator outcomeEvaluator;
    private StateUpdateService stateUpdateService;
    private InterventionOutcomeRepository interventionOutcomeRepository;
    private AuditService auditService;
private AgentEventPublisher eventPublisher;
    private AgentRunService agentRunService;

    private Invoice invoice;
    private Customer customer;
    private InvoiceContext context;
    private DiagnosisResult diagnosis;

    private final BigDecimal outstanding =
            new BigDecimal("10000.00");

    @BeforeEach
    void setUp() {

        invoiceRepository = mock(InvoiceRepository.class);
        agentRunRepository = mock(AgentRunRepository.class);
        agentRoundRepository = mock(AgentRoundRepository.class);

        observationService = mock(ObservationService.class);
        diagnosisService = mock(DiagnosisService.class);
        decisionEngine = mock(DecisionEngine.class);
        policyEngine = mock(PolicyEngine.class);
        simulationExecutionService =
                mock(SimulationExecutionService.class);
        outcomeEvaluator = mock(OutcomeEvaluator.class);
        stateUpdateService = mock(StateUpdateService.class);
        interventionOutcomeRepository = mock(InterventionOutcomeRepository.class);
        eventPublisher = mock(AgentEventPublisher.class);
        auditService = mock(AuditService.class);
        eventPublisher = mock(AgentEventPublisher.class);
        agentRunService = new AgentRunService(
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
                eventPublisher
        );

        customer = mock(Customer.class);

        when(customer.getExternalRef())
                .thenReturn("CUST-001");

        when(customer.isDoNotContact())
                .thenReturn(false);

        invoice = mock(Invoice.class);

        when(invoice.getId())
                .thenReturn(100L);

        when(invoice.getExternalRef())
                .thenReturn("INV-001");

        when(invoice.getCustomer())
                .thenReturn(customer);

        when(invoice.getOutstandingAmount())
                .thenReturn(outstanding);

        when(invoice.getIssueDate())
                .thenReturn(
                        LocalDate.now().minusDays(45));

        when(invoice.getDueDate())
                .thenReturn(
                        LocalDate.now().minusDays(15));

        when(invoice.getStatus())
                .thenReturn(InvoiceStatus.OPEN);

        when(invoiceRepository.findByExternalRef("INV-001"))
                .thenReturn(Optional.of(invoice));

        when(agentRunRepository.save(any(AgentRun.class)))
                .thenAnswer(invocation -> {
                    AgentRun run =
                            invocation.getArgument(0);
                    setId(run, 1L);
                    return run;
                });

        when(agentRoundRepository.save(any(AgentRound.class)))
                .thenAnswer(invocation -> {
                    AgentRound round =
                            invocation.getArgument(0);
                    setId(round, 10L);
                    return round;
                });

        context = mock(InvoiceContext.class);

        when(context.invoiceReference())
                .thenReturn("INV-001");

        when(context.customerReference())
                .thenReturn("CUST-001");

        when(context.outstandingAmount())
                .thenReturn(outstanding);

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

        diagnosis = mock(DiagnosisResult.class);

        when(diagnosis.category())
                .thenReturn(DiagnosisCategory.values()[0]);

        when(diagnosis.evidence())
                .thenReturn(List.of());

        when(diagnosisService.diagnose(any()))
                .thenReturn(diagnosis);
    }

    @Test
    void happyPathShouldCompleteOneAgentRound() {

        RankedStrategy ranked =
                ranked(
                        InterventionStrategy.EMAIL_REMINDER,
                        1
                );

        configureDecision(ranked);

        allow(InterventionStrategy.EMAIL_REMINDER);

        ExecutionResult execution =
                simulatedExecution();

        OutcomeResult outcome =
                noResponseOutcome();

        StateUpdateResult state =
                unchangedState();

        when(simulationExecutionService.execute(
                any(ExecutionRequest.class)))
                .thenReturn(execution);

        when(outcomeEvaluator.evaluate(
                eq(OutcomeType.NO_RESPONSE),
                eq(outstanding),
                eq(BigDecimal.ZERO)))
                .thenReturn(outcome);

        when(stateUpdateService.applyPayment(
                eq(100L),
                eq(outstanding),
                eq(BigDecimal.ZERO),
                eq("OPEN")))
                .thenReturn(state);

        AgentRunResult result =
                agentRunService.run("INV-001");

        assertThat(result)
                .isNotNull();

        verify(observationService)
                .observe(any());

        verify(diagnosisService)
                .diagnose(any());

        verify(decisionEngine)
                .decide(context);

        verify(policyEngine)
                .authorizeBestAvailable(
                        any(),
                        eq(context),
                        any());

        verify(simulationExecutionService)
                .execute(any(ExecutionRequest.class));

        verify(outcomeEvaluator)
                .evaluate(
                        eq(OutcomeType.NO_RESPONSE),
                        eq(outstanding),
                        eq(BigDecimal.ZERO));

        verify(stateUpdateService)
                .applyPayment(
                        eq(100L),
                        eq(outstanding),
                        eq(BigDecimal.ZERO),
                        eq("OPEN"));
    }

    @Test
    void geminiFailureShouldStopExecutionAndFailRun() {

        when(diagnosisService.diagnose(any()))
                .thenThrow(
                        new RuntimeException(
                                "Gemini unavailable"));

        assertThatThrownBy(
                () -> agentRunService.run("INV-001"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Gemini unavailable");

        verify(diagnosisService)
                .diagnose(any());

        verifyNoInteractions(decisionEngine);
        verifyNoInteractions(policyEngine);
        verifyNoInteractions(simulationExecutionService);
        verifyNoInteractions(outcomeEvaluator);
        verifyNoInteractions(stateUpdateService);

        verify(agentRunRepository).save(any(AgentRun.class));
    }

    @Test
    void policyBlockShouldPreventExecution() {

        RankedStrategy ranked =
                ranked(
                        InterventionStrategy.EMAIL_REMINDER,
                        1
                );

        configureDecision(ranked);

        when(policyEngine.authorizeBestAvailable(
                any(),
                eq(context),
                any()))
                .thenReturn(
                        PolicyResult.humanEscalation(
                                "Strategy blocked by policy"));

        AgentRunResult result =
                agentRunService.run("INV-001");

        assertThat(result)
                .isNotNull();

        verify(policyEngine)
                .authorizeBestAvailable(
                        any(),
                        eq(context),
                        any());

        verifyNoInteractions(simulationExecutionService);
        verifyNoInteractions(outcomeEvaluator);
        verifyNoInteractions(stateUpdateService);
    }

    @Test
    void fallbackStrategyShouldExecuteWhenFirstStrategyBlocked() {

        RankedStrategy first =
                ranked(
                        InterventionStrategy.EMAIL_REMINDER,
                        1
                );

        RankedStrategy second =
                ranked(
                        InterventionStrategy.PAYMENT_PLAN,
                        2
                );

        DecisionResult decision =
                new DecisionResult(
                        first,
                        List.of(first, second)
                );

        when(decisionEngine.decide(context))
                .thenReturn(decision);

        when(policyEngine.authorizeBestAvailable(
                any(),
                eq(context),
                any()))
                .thenReturn(
                        PolicyResult.allowed(
                                InterventionStrategy.PAYMENT_PLAN
                                        .name()));

        when(simulationExecutionService.execute(
                any(ExecutionRequest.class)))
                .thenAnswer(invocation ->
                        ExecutionResult.simulated(
                                invocation.getArgument(
                                        0,
                                        ExecutionRequest.class)));

        when(outcomeEvaluator.evaluate(
                eq(OutcomeType.NO_RESPONSE),
                eq(outstanding),
                eq(BigDecimal.ZERO)))
                .thenReturn(noResponseOutcome());

        when(stateUpdateService.applyPayment(
                eq(100L),
                eq(outstanding),
                eq(BigDecimal.ZERO),
                eq("OPEN")))
                .thenReturn(unchangedState());

        agentRunService.run("INV-001");

        ArgumentCaptor<ExecutionRequest>
                captor =
                ArgumentCaptor.forClass(
                        ExecutionRequest.class);

        verify(simulationExecutionService)
                .execute(captor.capture());

        assertThat(captor.getValue().strategy())
                .isEqualTo(
                        InterventionStrategy.PAYMENT_PLAN
                                .name());
    }

    @Test
    void allStrategiesBlockedShouldEscalateWithoutExecution() {

        RankedStrategy first =
                ranked(
                        InterventionStrategy.EMAIL_REMINDER,
                        1
                );

        RankedStrategy second =
                ranked(
                        InterventionStrategy.PAYMENT_PLAN,
                        2
                );

        DecisionResult decision =
                new DecisionResult(
                        first,
                        List.of(first, second)
                );

        when(decisionEngine.decide(context))
                .thenReturn(decision);

        when(policyEngine.authorizeBestAvailable(
                any(),
                eq(context),
                any()))
                .thenReturn(
                        PolicyResult.humanEscalation(
                                "All candidate strategies blocked"));

        AgentRunResult result =
                agentRunService.run("INV-001");

        assertThat(result)
                .isNotNull();

        verifyNoInteractions(simulationExecutionService);
        verifyNoInteractions(outcomeEvaluator);
        verifyNoInteractions(stateUpdateService);

        verify(agentRunRepository, atLeastOnce())
                .save(any(AgentRun.class));

        verify(agentRoundRepository, atLeastOnce())
                .save(any(AgentRound.class));
    }

    @Test
    void executionFailureShouldReachOutcomeEvaluation() {

        RankedStrategy ranked =
                ranked(
                        InterventionStrategy.EMAIL_REMINDER,
                        1
                );

        configureDecision(ranked);
        allow(InterventionStrategy.EMAIL_REMINDER);

        when(simulationExecutionService.execute(
                any(ExecutionRequest.class)))
                .thenAnswer(invocation -> {
                    ExecutionRequest request =
                            invocation.getArgument(
                                    0,
                                    ExecutionRequest.class);

                    return ExecutionResult.failed(
                            request,
                            "Execution failed");
                });

        OutcomeResult failureOutcome =
                new OutcomeResult(
                        OutcomeType.EXECUTION_FAILURE,
                        outstanding,
                        BigDecimal.ZERO,
                        outstanding,
                        BigDecimal.ZERO,
                        false,
                        true
                );

        when(outcomeEvaluator.evaluate(
                eq(OutcomeType.EXECUTION_FAILURE),
                eq(outstanding),
                eq(BigDecimal.ZERO)))
                .thenReturn(failureOutcome);

        when(stateUpdateService.applyPayment(
                eq(100L),
                eq(outstanding),
                eq(BigDecimal.ZERO),
                eq("OPEN")))
                .thenReturn(unchangedState());

        AgentRunResult result =
                agentRunService.run("INV-001");

        assertThat(result)
                .isNotNull();

        verify(outcomeEvaluator)
                .evaluate(
                        eq(OutcomeType.EXECUTION_FAILURE),
                        eq(outstanding),
                        eq(BigDecimal.ZERO));

        verify(stateUpdateService)
                .applyPayment(
                        eq(100L),
                        eq(outstanding),
                        eq(BigDecimal.ZERO),
                        eq("OPEN"));
    }

    @Test
    void partialPaymentShouldPropagateIntoStateUpdate() {

        RankedStrategy ranked =
                ranked(
                        InterventionStrategy.EMAIL_REMINDER,
                        1
                );

        configureDecision(ranked);
        allow(InterventionStrategy.EMAIL_REMINDER);

        when(simulationExecutionService.execute(
                any(ExecutionRequest.class)))
                .thenAnswer(invocation ->
                        ExecutionResult.simulated(
                                invocation.getArgument(
                                        0,
                                        ExecutionRequest.class)));

        BigDecimal paid =
                new BigDecimal("2500.00");

        BigDecimal remaining =
                new BigDecimal("7500.00");

        OutcomeResult partial =
                new OutcomeResult(
                        OutcomeType.PARTIAL_PAYMENT,
                        outstanding,
                        paid,
                        remaining,
                        new BigDecimal("0.25"),
                        true,
                        true
                );

        when(outcomeEvaluator.evaluate(
                eq(OutcomeType.NO_RESPONSE),
                eq(outstanding),
                eq(BigDecimal.ZERO)))
                .thenReturn(partial);

        StateUpdateResult updated =
                new StateUpdateResult(
                        100L,
                        outstanding,
                        remaining,
                        "OPEN",
                        "OPEN",
                        true
                );

        when(stateUpdateService.applyPayment(
                eq(100L),
                eq(outstanding),
                eq(paid),
                eq("OPEN")))
                .thenReturn(updated);

        agentRunService.run("INV-001");

        verify(stateUpdateService)
                .applyPayment(
                        eq(100L),
                        eq(outstanding),
                        eq(paid),
                        eq("OPEN"));
    }

    @Test
    void stateUpdateShouldPersistChangedInvoiceState() {

        RankedStrategy ranked =
                ranked(
                        InterventionStrategy.EMAIL_REMINDER,
                        1
                );

        configureDecision(ranked);
        allow(InterventionStrategy.EMAIL_REMINDER);

        when(simulationExecutionService.execute(
                any(ExecutionRequest.class)))
                .thenAnswer(invocation ->
                        ExecutionResult.simulated(
                                invocation.getArgument(
                                        0,
                                        ExecutionRequest.class)));

        OutcomeResult outcome =
                new OutcomeResult(
                        OutcomeType.PARTIAL_PAYMENT,
                        outstanding,
                        new BigDecimal("2500.00"),
                        new BigDecimal("7500.00"),
                        new BigDecimal("0.25"),
                        true,
                        true
                );

        when(outcomeEvaluator.evaluate(
                eq(OutcomeType.NO_RESPONSE),
                eq(outstanding),
                eq(BigDecimal.ZERO)))
                .thenReturn(outcome);

        StateUpdateResult state =
                new StateUpdateResult(
                        100L,
                        outstanding,
                        new BigDecimal("7500.00"),
                        "OPEN",
                        "OPEN",
                        true
                );

        when(stateUpdateService.applyPayment(
                eq(100L),
                eq(outstanding),
                eq(new BigDecimal("2500.00")),
                eq("OPEN")))
                .thenReturn(state);

        agentRunService.run("INV-001");

        verify(invoice)
                .setOutstandingAmount(
                        new BigDecimal("7500.00"));

        verify(invoiceRepository)
                .save(invoice);
    }

    @Test
    void auditShouldContainCompleteSingleRoundLifecycle() {

        RankedStrategy ranked =
                ranked(
                        InterventionStrategy.EMAIL_REMINDER,
                        1
                );

        configureDecision(ranked);
        allow(InterventionStrategy.EMAIL_REMINDER);

        when(simulationExecutionService.execute(
                any(ExecutionRequest.class)))
                .thenAnswer(invocation ->
                        ExecutionResult.simulated(
                                invocation.getArgument(
                                        0,
                                        ExecutionRequest.class)));

        when(outcomeEvaluator.evaluate(
                eq(OutcomeType.NO_RESPONSE),
                eq(outstanding),
                eq(BigDecimal.ZERO)))
                .thenReturn(noResponseOutcome());

        when(stateUpdateService.applyPayment(
                eq(100L),
                eq(outstanding),
                eq(BigDecimal.ZERO),
                eq("OPEN")))
                .thenReturn(unchangedState());

        agentRunService.run("INV-001");

        ArgumentCaptor<AuditEventType>
                captor =
                ArgumentCaptor.forClass(
                        AuditEventType.class);

        verify(auditService, times(13))
                .append(
                        eq(1L),
                        eq(10L),
                        captor.capture(),
                        eq("AGENT"),
                        anyString());

        assertThat(captor.getAllValues())
                .containsExactly(
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
    }

    private void configureDecision(
            RankedStrategy ranked) {

        when(decisionEngine.decide(context))
                .thenReturn(
                        new DecisionResult(
                                ranked,
                                List.of(ranked)));
    }

    private void allow(
            InterventionStrategy strategy) {

        when(policyEngine.authorizeBestAvailable(
                any(),
                eq(context),
                any()))
                .thenReturn(
                        PolicyResult.allowed(
                                strategy.name()));
    }

    private RankedStrategy ranked(
            InterventionStrategy strategy,
            int rank) {

        return new RankedStrategy(
                strategy,
                new BigDecimal("0.80"),
                outstanding,
                new BigDecimal("10.00"),
                new BigDecimal("7990.00"),
                rank
        );
    }

    private ExecutionResult simulatedExecution() {

        ExecutionRequest request =
                new ExecutionRequest(
                        "run-1-round-10",
                        "CUST-001",
                        "INV-001",
                        InterventionStrategy
                                .EMAIL_REMINDER
                                .name(),
                        outstanding,
                        true,
                        true
                );

        return ExecutionResult.simulated(request);
    }

    private OutcomeResult noResponseOutcome() {

        return new OutcomeResult(
                OutcomeType.NO_RESPONSE,
                outstanding,
                BigDecimal.ZERO,
                outstanding,
                BigDecimal.ZERO,
                false,
                true
        );
    }

    private StateUpdateResult unchangedState() {

        return new StateUpdateResult(
                100L,
                outstanding,
                outstanding,
                "OPEN",
                "OPEN",
                false
        );
    }

    private static void setId(
            Object entity,
            Long id) {

        try {
            var field =
                    entity.getClass()
                            .getDeclaredField("id");

            field.setAccessible(true);
            field.set(entity, id);

        } catch (Exception exception) {
            throw new RuntimeException(
                    "Unable to set test entity ID",
                    exception);
        }
    }
    @Test
void authorizedRoundShouldPublishCompleteEventLifecycle() {

    RankedStrategy ranked =
            ranked(
                    InterventionStrategy.EMAIL_REMINDER,
                    1
            );

    configureDecision(ranked);
    allow(InterventionStrategy.EMAIL_REMINDER);

    when(simulationExecutionService.execute(
            any(ExecutionRequest.class)))
            .thenAnswer(invocation ->
                    ExecutionResult.simulated(
                            invocation.getArgument(
                                    0,
                                    ExecutionRequest.class)));

    when(outcomeEvaluator.evaluate(
            eq(OutcomeType.NO_RESPONSE),
            eq(outstanding),
            eq(BigDecimal.ZERO)))
            .thenReturn(noResponseOutcome());

    when(stateUpdateService.applyPayment(
            eq(100L),
            eq(outstanding),
            eq(BigDecimal.ZERO),
            eq("OPEN")))
            .thenReturn(unchangedState());

    agentRunService.run("INV-001");

    ArgumentCaptor<AgentEventType> eventCaptor =
            ArgumentCaptor.forClass(AgentEventType.class);

    verify(eventPublisher, times(8))
            .publish(
                    eq(1L),
                    eq(10L),
                    eventCaptor.capture(),
                    anyMap()
            );

    assertThat(eventCaptor.getAllValues())
            .containsExactly(
                    AgentEventType.INVOICE_STARTED,
                    AgentEventType.OBSERVATION_CREATED,
                    AgentEventType.DIAGNOSIS_COMPLETED,
                    AgentEventType.DECISION_CALCULATED,
                    AgentEventType.DECISION_AUTHORIZED,
                    AgentEventType.EXECUTION_COMPLETED,
                    AgentEventType.OUTCOME_RECEIVED,
                    AgentEventType.INVOICE_COMPLETED
            );

    assertThat(eventCaptor.getAllValues())
            .doesNotContain(AgentEventType.POLICY_BLOCKED);
}
}
