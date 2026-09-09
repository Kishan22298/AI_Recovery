package com.recovery.ReceivablesGuard.agent;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recovery.ReceivablesGuard.audit.AuditEventType;
import com.recovery.ReceivablesGuard.audit.AuditService;
import com.recovery.ReceivablesGuard.decision.DecisionEngine;
import com.recovery.ReceivablesGuard.decision.DecisionResult;
import com.recovery.ReceivablesGuard.diagnosis.DiagnosisResult;
import com.recovery.ReceivablesGuard.diagnosis.DiagnosisService;
import com.recovery.ReceivablesGuard.domain.AgentRound;
import com.recovery.ReceivablesGuard.domain.AgentRoundStatus;
import com.recovery.ReceivablesGuard.domain.AgentRun;
import com.recovery.ReceivablesGuard.domain.AgentRunStatus;
import com.recovery.ReceivablesGuard.domain.InterventionOutcome;
import com.recovery.ReceivablesGuard.domain.Invoice;
import com.recovery.ReceivablesGuard.event.AgentEventPublisher;
import com.recovery.ReceivablesGuard.event.AgentEventType;
import com.recovery.ReceivablesGuard.execution.ExecutionRequest;
import com.recovery.ReceivablesGuard.execution.ExecutionResult;
import com.recovery.ReceivablesGuard.execution.ExecutionStatus;
import com.recovery.ReceivablesGuard.execution.SimulationExecutionService;
import com.recovery.ReceivablesGuard.observation.InvoiceContext;
import com.recovery.ReceivablesGuard.observation.ObservationInput;
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

@Service
public class AgentRunService {

    private final InvoiceRepository invoiceRepository;
    private final AgentRunRepository agentRunRepository;
    private final AgentRoundRepository agentRoundRepository;
    private final AgentEventPublisher eventPublisher;

    private final ObservationService observationService;
    private final DiagnosisService diagnosisService;
    private final DecisionEngine decisionEngine;
    private final PolicyEngine policyEngine;
    private final InterventionOutcomeRepository interventionOutcomeRepository;
    private final SimulationExecutionService simulationExecutionService;
    private final OutcomeEvaluator outcomeEvaluator;
    private final StateUpdateService stateUpdateService;
    

    private final AuditService auditService;

    public AgentRunService(
            InvoiceRepository invoiceRepository,
            AgentRunRepository agentRunRepository,
            AgentRoundRepository agentRoundRepository,
            ObservationService observationService,
            DiagnosisService diagnosisService,
            DecisionEngine decisionEngine,
            PolicyEngine policyEngine,
            SimulationExecutionService simulationExecutionService,
            OutcomeEvaluator outcomeEvaluator,
            StateUpdateService stateUpdateService,
            InterventionOutcomeRepository interventionOutcomeRepository,
            AuditService auditService,
                AgentEventPublisher eventPublisher
        ) {

        this.invoiceRepository = invoiceRepository;
        this.agentRunRepository = agentRunRepository;
        this.agentRoundRepository = agentRoundRepository;

        this.observationService = observationService;
        this.diagnosisService = diagnosisService;
        this.decisionEngine = decisionEngine;
        this.policyEngine = policyEngine;

        this.simulationExecutionService =
                simulationExecutionService;

        this.outcomeEvaluator =
                outcomeEvaluator;

        this.stateUpdateService =
                stateUpdateService;

        this.interventionOutcomeRepository =
                interventionOutcomeRepository;

        this.auditService =
                auditService;
        
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AgentRunResult run(String invoiceReference) {

        /*
         * ============================================================
         * 1. LOAD INVOICE
         * ============================================================
         */

        Invoice invoice =
                invoiceRepository
                        .findByExternalRef(invoiceReference)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invoice not found: "
                                                + invoiceReference
                                ));

        /*
         * ============================================================
         * 2. CREATE AGENT RUN
         * ============================================================
         */

        AgentRun agentRun =
                new AgentRun(
                        invoice,
                        1
                );

        agentRun.setStatus(
                AgentRunStatus.RUNNING
        );

        agentRun.setStartedAt(
                Instant.now()
        );

        agentRun =
                agentRunRepository.save(agentRun);

        return runRound(
                agentRun,
                invoice,
                1
        );
    }

    @Transactional
    public AgentRunResult runRound(
            AgentRun agentRun,
            Invoice invoice,
            int roundNumber) {
                
                LocalDateTime now =
                                LocalDateTime.now();

        try {

            /*
             * ========================================================
             * 3. CREATE AGENT ROUND
             * ========================================================
             *
             * The round is created BEFORE observation so every audit
             * event can be attached to the same round.
             */

            AgentRound round =
        new AgentRound(
                agentRun,
                roundNumber);

            round.setStartedAt(
                    Instant.now()
            );
            round =
                    agentRoundRepository.save(round);
            
                    eventPublisher.publish(
        agentRun.getId(),
        round.getId(),
        AgentEventType.INVOICE_STARTED,
        Map.of(
                "invoiceId", invoice.getId(),
                "invoiceReference", invoice.getExternalRef(),
                "roundNumber", roundNumber
        )
);

            /*
             * ========================================================
             * 4. BUILD OBSERVATION INPUT
             * ========================================================
             */

            ObservationInput observationInput =
                    buildObservationInput(invoice);

            /*
             * ========================================================
             * 5. OBSERVE
             * ========================================================
             */

            InvoiceContext context =
                    observationService.observe(
                            observationInput
                    );

                    eventPublisher.publish(
        agentRun.getId(),
        round.getId(),
        AgentEventType.OBSERVATION_CREATED,
        Map.of(
                "invoiceReference",
                context.invoiceReference(),

                "outstandingAmount",
                context.outstandingAmount(),

                "invoiceAgeDays",
                context.invoiceAgeDays(),

                "daysOverdue",
                context.daysOverdue()
        )
);

            appendAudit(
                    agentRun,
                    round,
                    AuditEventType.OBSERVED_CONTEXT,
                    jsonObject(
                            jsonField(
                                    "invoiceReference",
                                    context.invoiceReference()
                            ),
                            jsonField(
                                    "customerReference",
                                    context.customerReference()
                            ),
                            jsonField(
                                    "outstandingAmount",
                                    context.outstandingAmount()
                            ),
                            jsonField(
                                    "invoiceAgeDays",
                                    context.invoiceAgeDays()
                            ),
                            jsonField(
                                    "daysOverdue",
                                    context.daysOverdue()
                            ),
                            jsonField(
                                    "paymentHistoryRate",
                                    context.paymentHistoryRate()
                            ),
                            jsonField(
                                    "responsivenessRate",
                                    context.responsivenessRate()
                            ),
                            jsonField(
                                    "doNotContact",
                                    context.doNotContact()
                            )
                    )
            );
            
            /*
             * ========================================================
             * 6. DIAGNOSIS
             * ========================================================
             */

            Map<String, Object> diagnosisObservation =
                    buildDiagnosisObservation(context);

            DiagnosisResult diagnosis =
                    diagnosisService.diagnose(
                            diagnosisObservation
                    );
           eventPublisher.publish(
        agentRun.getId(),
        round.getId(),
        AgentEventType.DIAGNOSIS_COMPLETED,
        Map.of(
                "category",
                diagnosis.category().name(),

                "evidenceCount",
                diagnosis.evidence().size()
        )
);

            appendAudit(
                    agentRun,
                    round,
                    AuditEventType.DIAGNOSIS,
                    jsonObject(
                            jsonField(
                                    "category",
                                    diagnosis.category().name()
                            ),
                            jsonField(
                                    "evidenceCount",
                                    diagnosis.evidence().size()
                            )
                    )
            );

            /*
             * ========================================================
             * 7. EVIDENCE
             * ========================================================
             *
             * Do not store raw Gemini evidence in the audit log.
             * Store safe metadata only.
             */

            appendAudit(
                    agentRun,
                    round,
                    AuditEventType.EVIDENCE,
                    jsonObject(
                            jsonField(
                                    "evidenceCount",
                                    diagnosis.evidence().size()
                            ),
                            jsonField(
                                    "source",
                                    "DIAGNOSIS"
                            )
                    )
            );

            /*
             * ========================================================
             * 8. DETERMINISTIC DECISION
             * ========================================================
             *
             * Gemini diagnosis is intentionally NOT passed into the
             * DecisionEngine.
             *
             * DecisionEngine performs deterministic:
             *
             * propensity
             * expected value
             * ranking
             */

            DecisionResult decision =
                    decisionEngine.decide(context);
        
            eventPublisher.publish(
        agentRun.getId(),
        round.getId(),
        AgentEventType.DECISION_CALCULATED,
        Map.of(
                "candidateCount",
                decision.rankedStrategies().size(),

                "selectedStrategy",
                decision.selectedStrategy() == null
                        ? "NONE"
                        : decision.selectedStrategy()
                                .strategy()
                                .name()
        )
);

            /*
             * ========================================================
             * 9. CANDIDATE STRATEGIES
             * ========================================================
             */

            List<String> rankedStrategies =
                    decision.rankedStrategies()
                            .stream()
                            .map(ranked ->
                                    ranked.strategy().name())
                            .toList();

            appendAudit(
                    agentRun,
                    round,
                    AuditEventType.CANDIDATE_STRATEGIES,
                    jsonObject(
                            jsonField(
                                    "strategies",
                                    rankedStrategies
                            )
                    )
            );

            /*
             * ========================================================
             * 10. PROPENSITY
             * ========================================================
             */

            if (decision.selectedStrategy() != null) {

                appendAudit(
                        agentRun,
                        round,
                        AuditEventType.PROPENSITY,
                        jsonObject(
                                jsonField(
                                        "selectedStrategy",
                                        decision.selectedStrategy()
                                                .strategy()
                                                .name()
                                ),
                                jsonField(
                                        "probabilityOfSuccess",
                                        decision.selectedStrategy()
                                                .probabilityOfSuccess()
                                )
                        )
                );

            } else {

                appendAudit(
                        agentRun,
                        round,
                        AuditEventType.PROPENSITY,
                        jsonObject(
                                jsonField(
                                        "selectedStrategy",
                                        null
                                )
                        )
                );
            }

            /*
             * ========================================================
             * 11. EXPECTED VALUE
             * ========================================================
             */

            appendAudit(
                    agentRun,
                    round,
                    AuditEventType.EXPECTED_VALUE,
                    buildExpectedValueAuditData(
                            decision
                    )
            );

            /*
             * ========================================================
             * 12. RANKING
             * ========================================================
             */

            appendAudit(
                    agentRun,
                    round,
                    AuditEventType.RANKING,
                    buildRankingAuditData(
                            decision
                    )
            );

            /*
             * ========================================================
             * 13. POLICY AUTHORIZATION
             * ========================================================
             */

            PolicyResult policy =
                    policyEngine.authorizeBestAvailable(
                            rankedStrategies,
                            context,
                            now
                    );
            
        if (policy.selectedStrategy() != null) {

    eventPublisher.publish(
            agentRun.getId(),
            round.getId(),
            AgentEventType.DECISION_AUTHORIZED,
            Map.of(
                    "strategy",
                    policy.selectedStrategy(),
                    "decision",
                    policy.decision().name()
            )
    );

} 

            /*
             * ========================================================
             * 14. POLICY AUDIT
             * ========================================================
             */

            appendAudit(
                    agentRun,
                    round,
                    AuditEventType.POLICY,
                    jsonObject(
                            jsonField(
                                    "decision",
                                    policy.decision().name()
                            ),
                            jsonField(
                                    "selectedStrategy",
                                    policy.selectedStrategy()
                            ),
                            jsonField(
                                    "reason",
                                    policy.reason()
                            )
                    )
            );
        //     eventPublisher.publish(
        // agentRun.getId(),
        // round.getId(),
        // AgentEventType.POLICY_BLOCKED,
        // Map.of(
        //         "reason",
        //         policy.reason()
        // )
//);
            /*
             * ========================================================
             * 15. BLOCKED ACTIONS
             * ========================================================
             */

            appendAudit(
                    agentRun,
                    round,
                    AuditEventType.BLOCKED_ACTIONS,
                    jsonObject(
                            jsonField(
                                    "blockedRules",
                                    policy.blockedRules()
                            ),
                            jsonField(
                                    "count",
                                    policy.blockedRules().size()
                            )
                    )
            );

            /*
             * ========================================================
             * 16. UPDATE ROUND WITH DIAGNOSIS / PROPENSITY
             * ========================================================
             */

            round.setDiagnosisCategory(
                    diagnosis.category().name()
            );

            if (decision.selectedStrategy() != null) {

                round.setPropensityScore(
                        decision.selectedStrategy()
                                .probabilityOfSuccess()
                );
            }

            /*
             * ========================================================
             * 17. POLICY BLOCKED / HUMAN ESCALATION
             * ========================================================
             */

            if (policy.selectedStrategy() == null) {
                
                eventPublisher.publish(
        agentRun.getId(),
        round.getId(),
        AgentEventType.POLICY_BLOCKED,
        Map.of(
                "reason",
                policy.reason()
        )
);
                round.setStatus(
                        AgentRoundStatus.BLOCKED
                );

                round.setAuthorized(false);

                round.setCompletedAt(
                        Instant.now()
                );

                round =
                        agentRoundRepository.save(round);

                /*
                 * FINAL DECISION
                 */

                appendAudit(
                        agentRun,
                        round,
                        AuditEventType.FINAL_DECISION,
                        jsonObject(
                                jsonField(
                                        "decision",
                                        "BLOCKED"
                                ),
                                jsonField(
                                        "selectedStrategy",
                                        null
                                ),
                                jsonField(
                                        "reason",
                                        policy.reason()
                                )
                        )
                );

                /*
                 * EXECUTION
                 *
                 * No external execution occurs because policy blocked
                 * the action. We still record the lifecycle stage.
                 */

                appendAudit(
                        agentRun,
                        round,
                        AuditEventType.EXECUTION,
                        jsonObject(
                                jsonField(
                                        "status",
                                        "SKIPPED"
                                ),
                                jsonField(
                                        "reason",
                                        "POLICY_BLOCKED"
                                )
                        )
                );

                /*
                 * OUTCOME
                 */

                appendAudit(
                        agentRun,
                        round,
                        AuditEventType.OUTCOME,
                        jsonObject(
                                jsonField(
                                        "status",
                                        "SKIPPED"
                                ),
                                jsonField(
                                        "reason",
                                        "POLICY_BLOCKED"
                                )
                        )
                );

                /*
                 * STATE UPDATE
                 *
                 * No payment occurred, therefore no invoice state
                 * mutation was performed.
                 */

                appendAudit(
                        agentRun,
                        round,
                        AuditEventType.STATE_UPDATE,
                        jsonObject(
                                jsonField(
                                        "stateChanged",
                                        false
                                ),
                                jsonField(
                                        "reason",
                                        "POLICY_BLOCKED"
                                )
                        )
                );

                // agentRun.setStatus(
                //         AgentRunStatus.COMPLETED
                // );

                // agentRun.setCompletedAt(
                //         Instant.now()
                // );

                // agentRunRepository.save(agentRun);
                eventPublisher.publish(
        agentRun.getId(),
        round.getId(),
        AgentEventType.INVOICE_COMPLETED,
        Map.of(
                "status",
                "BLOCKED"
        )
);
                return AgentRunResult.blocked(
                        agentRun.getId(),
                        round.getId(),
                        invoice.getId(),
                        diagnosis,
                        decision,
                        policy
                );
            }

            /*
             * ========================================================
             * 18. STORE AUTHORIZED STRATEGY
             * ========================================================
             */

            round.setSelectedStrategy(
                    policy.selectedStrategy()
            );

            round.setAuthorized(true);

            round.setStatus(
                    AgentRoundStatus.DECIDED
            );

            round =
                    agentRoundRepository.save(round);

            /*
             * ========================================================
             * 19. FINAL DECISION
             * ========================================================
             */

            appendAudit(
                    agentRun,
                    round,
                    AuditEventType.FINAL_DECISION,
                    jsonObject(
                            jsonField(
                                    "decision",
                                    "AUTHORIZED"
                            ),
                            jsonField(
                                    "selectedStrategy",
                                    policy.selectedStrategy()
                            ),
                            jsonField(
                                    "reason",
                                    policy.reason()
                            )
                    )
            );

            /*
             * ========================================================
             * 20. SIMULATED EXECUTION
             * ========================================================
             */

            String executionId =
                    "run-"
                            + agentRun.getId()
                            + "-round-"
                            + round.getId();

            ExecutionRequest executionRequest =
                    new ExecutionRequest(
                            executionId,
                            invoice.getCustomer()
                                    .getExternalRef(),
                            invoice.getExternalRef(),
                            policy.selectedStrategy(),
                            invoice.getOutstandingAmount(),
                            true,
                            true
                    );

            round.setStatus(
                    AgentRoundStatus.EXECUTED
            );

            round =
                    agentRoundRepository.save(round);

            ExecutionResult executionResult =
                    simulationExecutionService.execute(
                            executionRequest
                    );
           eventPublisher.publish(
        agentRun.getId(),
        round.getId(),
        AgentEventType.EXECUTION_COMPLETED,
        Map.of(
                "executionId",
                executionResult.executionId(),

                "status",
                executionResult.status().name(),

                "strategy",
                executionResult.strategy()
        )
);

            /*
             * ========================================================
             * 21. EXECUTION AUDIT
             * ========================================================
             */

            appendAudit(
                    agentRun,
                    round,
                    AuditEventType.EXECUTION,
                    jsonObject(
                            jsonField(
                                    "executionId",
                                    executionResult.executionId()
                            ),
                            jsonField(
                                    "status",
                                    executionResult.status().name()
                            ),
                            jsonField(
                                    "strategy",
                                    executionResult.strategy()
                            ),
                            jsonField(
                                    "message",
                                    executionResult.message()
                            )
                    )
            );

            /*
             * ========================================================
             * 22. EVALUATE OUTCOME
             * ========================================================
             *
             * Simulation does not fabricate payment.
             * Therefore SIMULATED -> NO_RESPONSE.
             */

            OutcomeType outcomeType =
                    executionResult.status()
                            == ExecutionStatus.SIMULATED
                            ? OutcomeType.NO_RESPONSE
                            : OutcomeType.EXECUTION_FAILURE;

            OutcomeResult outcome =
                    outcomeEvaluator.evaluate(
                            outcomeType,
                            invoice.getOutstandingAmount(),
                            BigDecimal.ZERO
                    );
        eventPublisher.publish(
        agentRun.getId(),
        round.getId(),
        AgentEventType.OUTCOME_RECEIVED,
        Map.of(
                "outcomeType",
                outcome.outcomeType().name(),

                "paidAmount",
                outcome.paidAmount(),

                "remainingAmount",
                outcome.remainingAmount(),

                "successful",
                outcome.successful()
        )
);
           InterventionOutcome interventionOutcome =
        new InterventionOutcome(
                round,
                invoice,
                com.recovery.ReceivablesGuard.domain.OutcomeType
                        .valueOf(
                                outcome.outcomeType().name()
                        ),
                outcome.paidAmount(),
                outcome.successful()
                        ? "Successful intervention outcome"
                        : "Intervention outcome recorded",
                Instant.now()
        );

interventionOutcomeRepository.save(
        interventionOutcome
);

interventionOutcomeRepository.save(
        interventionOutcome
);

            /*
             * ========================================================
             * 23. OUTCOME AUDIT
             * ========================================================
             */

            appendAudit(
                    agentRun,
                    round,
                    AuditEventType.OUTCOME,
                    jsonObject(
                            jsonField(
                                    "outcomeType",
                                    outcome.outcomeType().name()
                            ),
                            jsonField(
                                    "invoiceAmount",
                                    outcome.invoiceAmount()
                            ),
                            jsonField(
                                    "paidAmount",
                                    outcome.paidAmount()
                            ),
                            jsonField(
                                    "remainingAmount",
                                    outcome.remainingAmount()
                            ),
                            jsonField(
                                    "recoveryRate",
                                    outcome.recoveryRate()
                            ),
                            jsonField(
                                    "successful",
                                    outcome.successful()
                            ),
                            jsonField(
                                    "requiresFollowUp",
                                    outcome.requiresFollowUp()
                            )
                    )
            );

            /*
             * ========================================================
             * 24. UPDATE STATE
             * ========================================================
             */

            StateUpdateResult stateResult =
                    stateUpdateService.applyPayment(
                            invoice.getId(),
                            invoice.getOutstandingAmount(),
                            outcome.paidAmount(),
                            invoice.getStatus().name()
                    );

            /*
             * ========================================================
             * 25. STATE UPDATE AUDIT
             * ========================================================
             */

            appendAudit(
                    agentRun,
                    round,
                    AuditEventType.STATE_UPDATE,
                    jsonObject(
                            jsonField(
                                    "invoiceId",
                                    stateResult.invoiceId()
                            ),
                            jsonField(
                                    "previousOutstandingAmount",
                                    stateResult.previousOutstandingAmount()
                            ),
                            jsonField(
                                    "newOutstandingAmount",
                                    stateResult.newOutstandingAmount()
                            ),
                            jsonField(
                                    "previousInvoiceStatus",
                                    stateResult.previousInvoiceStatus()
                            ),
                            jsonField(
                                    "newInvoiceStatus",
                                    stateResult.newInvoiceStatus()
                            ),
                            jsonField(
                                    "stateChanged",
                                    stateResult.stateChanged()
                            )
                    )
            );

            /*
             * ========================================================
             * 26. APPLY INVOICE STATE
             * ========================================================
             */

            if (stateResult.stateChanged()) {

                invoice.setOutstandingAmount(
                        stateResult.newOutstandingAmount()
                );

               invoice.setStatus(
        switch (stateResult.newInvoiceStatus()) {
            case "OUTSTANDING" ->
                    com.recovery.ReceivablesGuard.domain.InvoiceStatus.OPEN;

            default ->
                    com.recovery.ReceivablesGuard.domain.InvoiceStatus
                            .valueOf(stateResult.newInvoiceStatus());
        }
);

                invoiceRepository.save(invoice);
            }

            /*
             * ========================================================
             * 27. COMPLETE ROUND
             * ========================================================
             */

            round.setStatus(
                    AgentRoundStatus.COMPLETED
            );

            round.setCompletedAt(
                    Instant.now()
            );


            agentRoundRepository.save(round);
            /*
 * ========================================================
 * 28. COMPLETE INVOICE ROUND
 * ========================================================
 *
 * AgentRunManager owns the overall agent-run lifecycle.
 * AgentRunService only completes the current invoice round.
 */

eventPublisher.publish(
        agentRun.getId(),
        round.getId(),
        AgentEventType.INVOICE_COMPLETED,
        Map.of(
                "invoiceId",
                invoice.getId(),

                "invoiceReference",
                invoice.getExternalRef(),

                "outstandingAmount",
                invoice.getOutstandingAmount()
        )
);
            /*
             * ========================================================
             * 29. RETURN RESULT
             * ========================================================
             */

            return AgentRunResult.completed(
                    agentRun.getId(),
                    round.getId(),
                    invoice.getId(),
                    diagnosis,
                    decision,
                    policy,
                    executionResult,
                    outcome,
                    stateResult
            );

        } catch (Exception exception) {

    /*
     * AgentRunManager owns the overall AgentRun lifecycle.
     * Propagate the exception so the manager can determine
     * whether the run becomes FAILED or CANCELLED.
     */

    throw exception;
}
    }

    /*
     * ================================================================
     * AUDIT HELPER
     * ================================================================
     */

    private void appendAudit(
            AgentRun agentRun,
            AgentRound round,
            AuditEventType eventType,
            String eventData) {

        auditService.append(
                agentRun.getId(),
                round.getId(),
                eventType,
                "AGENT",
                eventData
        );
    }

    /*
     * ================================================================
     * EXPECTED VALUE AUDIT DATA
     * ================================================================
     */

    private String buildExpectedValueAuditData(
            DecisionResult decision) {

        StringBuilder builder =
                new StringBuilder();

        builder.append("{\"strategies\":[");

        for (int i = 0;
             i < decision.rankedStrategies().size();
             i++) {

            if (i > 0) {
                builder.append(",");
            }

            var ranked =
                    decision.rankedStrategies().get(i);

            builder.append(
                    jsonObject(
                            jsonField(
                                    "strategy",
                                    ranked.strategy().name()
                            ),
                            jsonField(
                                    "probabilityOfSuccess",
                                    ranked.probabilityOfSuccess()
                            ),
                            jsonField(
                                    "outstandingAmount",
                                    ranked.outstandingAmount()
                            ),
                            jsonField(
                                    "interventionCost",
                                    ranked.interventionCost()
                            ),
                            jsonField(
                                    "expectedValue",
                                    ranked.expectedValue()
                            )
                    )
            );
        }

        builder.append("]}");

        return builder.toString();
    }

    /*
     * ================================================================
     * RANKING AUDIT DATA
     * ================================================================
     */

    private String buildRankingAuditData(
            DecisionResult decision) {

        StringBuilder builder =
                new StringBuilder();

        builder.append("{\"ranking\":[");

        for (int i = 0;
             i < decision.rankedStrategies().size();
             i++) {

            if (i > 0) {
                builder.append(",");
            }

            var ranked =
                    decision.rankedStrategies().get(i);

            builder.append(
                    jsonObject(
                            jsonField(
                                    "strategy",
                                    ranked.strategy().name()
                            ),
                            jsonField(
                                    "rank",
                                    ranked.rank()
                            ),
                            jsonField(
                                    "expectedValue",
                                    ranked.expectedValue()
                            )
                    )
            );
        }

        builder.append("]}");

        return builder.toString();
    }

    /*
     * ================================================================
     * SIMPLE JSON HELPERS
     * ================================================================
     *
     * We intentionally avoid adding another dependency just for
     * audit serialization.
     */

    private String jsonObject(
            String... fields) {

        return "{"
                + String.join(",", fields)
                + "}";
    }

    private String jsonField(
            String name,
            Object value) {

        return "\""
                + escapeJson(name)
                + "\":"
                + jsonValue(value);
    }

    private String jsonValue(
            Object value) {

        if (value == null) {
            return "null";
        }

        if (value instanceof Number
                || value instanceof Boolean) {

            return String.valueOf(value);
        }

        if (value instanceof List<?> list) {

            StringBuilder builder =
                    new StringBuilder();

            builder.append("[");

            for (int i = 0;
                 i < list.size();
                 i++) {

                if (i > 0) {
                    builder.append(",");
                }

                builder.append(
                        jsonValue(
                                list.get(i)
                        )
                );
            }

            builder.append("]");

            return builder.toString();
        }

        return "\""
                + escapeJson(
                        String.valueOf(value)
                )
                + "\"";
    }

    private String escapeJson(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    /*
     * ================================================================
     * OBSERVATION INPUT
     * ================================================================
     */

    private ObservationInput buildObservationInput(
            Invoice invoice) {

        return new ObservationInput(

                // 1
                invoice.getExternalRef(),

                // 2
                invoice.getCustomer().getExternalRef(),

                // 3
                invoice.getOutstandingAmount(),

                // 4
                invoice.getIssueDate(),

                // 5
                invoice.getDueDate(),

                // 6
                null,

                // 7
                null,

                // 8
                null,

                // 9
                null,

                // 10
                null,

                // 11
                null,

                // 12
                null,

                // 13
                null,

                // 14
                null,

                // 15
                null,

                // 16
                null,

                // 17
                null,

                // 18
                null,

                // 19
                null,

                // 20
                null,

                // 21
                null,

                // 22
                null,

                // 23
                invoice.getCustomer().isDoNotContact(),

                // 24
                null,

                // 25
                null
        );
    }

    /*
     * ================================================================
     * DIAGNOSIS OBSERVATION
     * ================================================================
     */

    private Map<String, Object> buildDiagnosisObservation(
            InvoiceContext context) {

        return Map.ofEntries(

                Map.entry(
                        "invoiceReference",
                        context.invoiceReference()
                ),

                Map.entry(
                        "customerReference",
                        context.customerReference()
                ),

                Map.entry(
                        "outstandingAmount",
                        context.outstandingAmount()
                ),

                Map.entry(
                        "invoiceAgeDays",
                        context.invoiceAgeDays()
                ),

                Map.entry(
                        "daysOverdue",
                        context.daysOverdue()
                ),

                Map.entry(
                        "historicalInvoiceCount",
                        context.historicalInvoiceCount()
                ),

                Map.entry(
                        "historicalPaidInvoiceCount",
                        context.historicalPaidInvoiceCount()
                ),

                Map.entry(
                        "historicalLatePaymentCount",
                        context.historicalLatePaymentCount()
                ),

                Map.entry(
                        "paymentHistoryRate",
                        context.paymentHistoryRate()
                ),

                Map.entry(
                        "responsivenessRate",
                        context.responsivenessRate()
                ),

                Map.entry(
                        "promiseCount",
                        context.promiseCount()
                ),

                Map.entry(
                        "brokenPromiseCount",
                        context.brokenPromiseCount()
                ),

                Map.entry(
                        "disputeCount",
                        context.disputeCount()
                ),

                Map.entry(
                        "previousInterventionCount",
                        context.previousInterventionCount()
                ),

                Map.entry(
                        "successfulInterventionCount",
                        context.successfulInterventionCount()
                ),

                Map.entry(
                        "doNotContact",
                        context.doNotContact()
                )
        );
    }

//     void runRound(AgentRun savedRun, Invoice invoice, int i) {
//         throw new UnsupportedOperationException("Not supported yet.");
//     }
}