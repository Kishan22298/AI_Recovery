package com.recovery.ReceivablesGuard.agent;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recovery.ReceivablesGuard.domain.AgentRun;
import com.recovery.ReceivablesGuard.domain.AgentRunStatus;
import com.recovery.ReceivablesGuard.repository.AgentRunRepository;

@Service
public class AgentRunCancellationService {

    private final AgentRunRepository agentRunRepository;
    private final BatchAgentRunner batchAgentRunner;

    public AgentRunCancellationService(
            AgentRunRepository agentRunRepository,
            BatchAgentRunner batchAgentRunner) {

        this.agentRunRepository = agentRunRepository;
        this.batchAgentRunner = batchAgentRunner;
    }

    @Transactional
    public boolean cancel(
            Long agentRunId) {

        if (agentRunId == null) {

            throw new IllegalArgumentException(
                    "agentRunId must not be null");
        }

        AgentRun agentRun =
                agentRunRepository
                        .findById(agentRunId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Agent run not found: "
                                                + agentRunId
                                ));

        AgentRunStatus status =
                agentRun.getStatus();

        if (status == AgentRunStatus.COMPLETED ||
                status == AgentRunStatus.FAILED ||
                status == AgentRunStatus.CANCELLED) {

            return false;
        }

        if (status != AgentRunStatus.RUNNING) {

            return false;
        }

        boolean cancelled =
                batchAgentRunner.cancel(
                        agentRunId
                );

        if (!cancelled) {

            return false;
        }

        /*
         * Persist cancellation immediately from the REST
         * cancellation request.
         *
         * AgentRunManager also handles interruption on the worker
         * side, so the lifecycle remains CANCELLED.
         */
        agentRun.setStatus(
                AgentRunStatus.CANCELLED
        );

        agentRun.setCompletedAt(
                Instant.now()
        );

        agentRunRepository.save(agentRun);

        return true;
    }
}