package com.recovery.ReceivablesGuard.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.recovery.ReceivablesGuard.agent.AgentRunCancellationService;
import com.recovery.ReceivablesGuard.agent.AgentRunManager;
import com.recovery.ReceivablesGuard.agent.BatchAgentRunner;
import com.recovery.ReceivablesGuard.api.dto.AgentRoundResponse;
import com.recovery.ReceivablesGuard.api.dto.AgentRunResponse;
import com.recovery.ReceivablesGuard.domain.AgentRun;
import com.recovery.ReceivablesGuard.repository.AgentRoundRepository;
import com.recovery.ReceivablesGuard.repository.AgentRunRepository;

@RestController
@RequestMapping("/api/agent-runs")
public class AgentRunController {

    private final AgentRunManager agentRunManager;
    private final AgentRunRepository agentRunRepository;
    private final AgentRunCancellationService agentRunCancellationService;
    private final AgentRoundRepository agentRoundRepository;
    private final BatchAgentRunner batchAgentRunner;

    public AgentRunController(
            AgentRunManager agentRunManager,
            AgentRunRepository agentRunRepository,
            AgentRunCancellationService agentRunCancellationService,
            AgentRoundRepository agentRoundRepository,
            BatchAgentRunner batchAgentRunner) {

        this.agentRunManager = agentRunManager;
        this.agentRunRepository = agentRunRepository;
        this.agentRunCancellationService =
                agentRunCancellationService;
        this.agentRoundRepository =
                agentRoundRepository;
        this.batchAgentRunner =
                batchAgentRunner;
    }

    @PostMapping
    public ResponseEntity<AgentRunResponse> run(
            @RequestParam String invoiceReference,
            @RequestParam(defaultValue = "1") int maxRounds) {

        /*
         * Create exactly ONE AgentRun and persist it as RUNNING.
         * The ID is available immediately.
         */
        AgentRun agentRun =
                agentRunManager.createRun(
                        invoiceReference,
                        maxRounds
                );

        /*
         * Start the execution asynchronously using the same
         * persisted AgentRun.
         */
        batchAgentRunner.startAsync(
                agentRun
        );

        /*
         * Return immediately instead of waiting for all rounds.
         */
        return ResponseEntity.ok(
                AgentRunResponse.from(agentRun)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgentRunResponse> getRun(
            @PathVariable Long id) {

        return agentRunRepository
                .findById(id)
                .map(agentRun ->
                        ResponseEntity.ok(
                                AgentRunResponse.from(
                                        agentRun
                                )
                        )
                )
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    @GetMapping("/{id}/rounds")
    public ResponseEntity<List<AgentRoundResponse>> getRounds(
            @PathVariable Long id) {

        if (!agentRunRepository.existsById(id)) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        List<AgentRoundResponse> rounds =
                agentRoundRepository
                        .findAllByAgentRunId(id)
                        .stream()
                        .map(AgentRoundResponse::from)
                        .toList();

        return ResponseEntity.ok(rounds);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<String> cancel(
            @PathVariable Long id) {

        boolean cancelled =
                agentRunCancellationService.cancel(id);

        if (!cancelled) {

            return ResponseEntity
                    .status(409)
                    .body(
                            "Agent run cannot be cancelled"
                    );
        }

        return ResponseEntity.ok(
                "Agent run cancelled"
        );
    }
}