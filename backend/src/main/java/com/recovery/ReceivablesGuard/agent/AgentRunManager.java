package com.recovery.ReceivablesGuard.agent;

import java.time.Instant;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recovery.ReceivablesGuard.domain.AgentRun;
import com.recovery.ReceivablesGuard.domain.AgentRunStatus;
import com.recovery.ReceivablesGuard.domain.Invoice;
import com.recovery.ReceivablesGuard.event.AgentEventPublisher;
import com.recovery.ReceivablesGuard.event.AgentEventType;
import com.recovery.ReceivablesGuard.repository.AgentRunRepository;
import com.recovery.ReceivablesGuard.repository.InvoiceRepository;

@Service
public class AgentRunManager {

    private final InvoiceRepository invoiceRepository;
    private final AgentRunRepository agentRunRepository;
    private final AgentRunService agentRunService;
    private final AgentEventPublisher eventPublisher;

    public AgentRunManager(
            InvoiceRepository invoiceRepository,
            AgentRunRepository agentRunRepository,
            AgentRunService agentRunService,
            AgentEventPublisher eventPublisher) {

        this.invoiceRepository = invoiceRepository;
        this.agentRunRepository = agentRunRepository;
        this.agentRunService = agentRunService;
        this.eventPublisher = eventPublisher;   
    }

    @Transactional
public AgentRun createRun(
        String invoiceReference,
        int maxRounds) {

    validateInput(invoiceReference, maxRounds);

    Invoice invoice =
            invoiceRepository
                    .findByExternalRef(invoiceReference)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Invoice not found: "
                                            + invoiceReference
                            ));

    AgentRun agentRun =
            new AgentRun(
                    invoice,
                    maxRounds
            );

    agentRun.setStatus(
            AgentRunStatus.RUNNING
    );

    agentRun.setStartedAt(
            Instant.now()
    );

    agentRun =
            agentRunRepository.save(agentRun);

    eventPublisher.publish(
            agentRun.getId(),
            null,
            AgentEventType.RUN_STARTED,
            Map.of(
                    "invoiceId",
                    invoice.getId(),

                    "invoiceReference",
                    invoice.getExternalRef(),

                    "maxRounds",
                    maxRounds
            )
    );

    return agentRun;
}

    @Transactional
    public AgentRunResult executeRun(
            AgentRun agentRun) {
              
        if (agentRun == null) {
    throw new IllegalArgumentException("Agent run must not be null");
}

if (agentRun.getStatus() == AgentRunStatus.CREATED) {
    agentRun.setStatus(AgentRunStatus.RUNNING);

    if (agentRun.getStartedAt() == null) {
        agentRun.setStartedAt(Instant.now());
    }

    agentRunRepository.save(agentRun);
}

if (agentRun.getStatus() != AgentRunStatus.RUNNING) {
    throw new IllegalStateException("Agent run is not RUNNING");
}

        AgentRunResult lastResult = null;

        try {

            for (int roundNumber = 1;
                 roundNumber <= agentRun.getMaxRounds();
                 roundNumber++) {

                /*
                 * Cancellation is cooperative.
                 *
                 * BatchAgentRunner interrupts the worker thread.
                 * This check prevents another round from starting
                 * after cancellation.
                 */
                if (Thread.currentThread().isInterrupted()) {

                    cancelRun(agentRun);

                    return lastResult;
                }

                Invoice invoice =
                        invoiceRepository
                                .findById(
                                        agentRun
                                                .getInvoice()
                                                .getId()
                                )
                                .orElseThrow(() ->
                                        new IllegalStateException(
                                                "Invoice disappeared during agent run"
                                        ));

lastResult =
        agentRunService.runRound(
                agentRun,
                invoice,
                roundNumber
        );

                /*
                 * If cancellation happened while the round was
                 * executing, do not mark the run COMPLETED.
                 */
                if (Thread.currentThread().isInterrupted()) {

                    cancelRun(agentRun);

                    return lastResult;
                }

                /*
                 * A blocked round means the policy layer has
                 * terminated the decision process.
                 */
                if (lastResult.isBlocked()) {
                    break;
                }

                /*
                 * Fully paid invoice: no reason to start another
                 * recovery round.
                 */
                if (invoice.getOutstandingAmount()
                        .signum() == 0) {
                    break;
                }
            }

            /*
             * Final cancellation check before COMPLETED.
             */
            if (Thread.currentThread().isInterrupted()) {

                cancelRun(agentRun);

                return lastResult;
            }

            agentRun.setStatus(
        AgentRunStatus.COMPLETED
);

agentRun.setCompletedAt(
        Instant.now()
);

agentRunRepository.save(agentRun);

eventPublisher.publish(
        agentRun.getId(),
        null,
        AgentEventType.RUN_COMPLETED,
        Map.of(
                "status",
                agentRun.getStatus().name()
        )
);

return lastResult;

        } catch (Exception exception) {

            /*
             * If the worker was interrupted, cancellation wins
             * over FAILED.
             */
            if (Thread.currentThread().isInterrupted()) {

                cancelRun(agentRun);

            } else {

                agentRun.setStatus(
                        AgentRunStatus.FAILED
                );

                agentRun.setCompletedAt(
                        Instant.now()
                );

                agentRunRepository.save(agentRun);
            }

            throw exception;
        }
    }

    /*
     * Compatibility wrapper for existing callers and tests.
     *
     * Existing synchronous code can continue calling run().
     */
    @Transactional
    public AgentRunResult run(
            String invoiceReference,
            int maxRounds) {

        AgentRun agentRun =
                createRun(
                        invoiceReference,
                        maxRounds
                );

        return executeRun(agentRun);
    }

    private void validateInput(
            String invoiceReference,
            int maxRounds) {

        if (invoiceReference == null ||
                invoiceReference.isBlank()) {

            throw new IllegalArgumentException(
                    "Invoice reference must not be blank"
            );
        }

        if (maxRounds <= 0) {

            throw new IllegalArgumentException(
                    "maxRounds must be greater than zero"
            );
        }
    }

    private void cancelRun(
            AgentRun agentRun) {

        agentRun.setStatus(
                AgentRunStatus.CANCELLED
        );

        agentRun.setCompletedAt(
                Instant.now()
        );

        agentRunRepository.save(agentRun);
    }
    @Transactional(readOnly = true)
public AgentRun loadRun(Long agentRunId) {

    if (agentRunId == null) {
        throw new IllegalArgumentException("Agent run ID must not be null");
    }

    return agentRunRepository
            .findById(agentRunId)
            .orElseThrow(() ->
                    new IllegalArgumentException(
                            "Agent run not found: " + agentRunId
                    )
            );
}
}