package com.recovery.ReceivablesGuard.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recovery.ReceivablesGuard.api.dto.AgentRoundResponse;
import com.recovery.ReceivablesGuard.repository.AgentRoundRepository;

@RestController
@RequestMapping("/api/agent-rounds")
public class AgentRoundController {

    private final AgentRoundRepository agentRoundRepository;

    public AgentRoundController(
            AgentRoundRepository agentRoundRepository) {

        this.agentRoundRepository =
                agentRoundRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgentRoundResponse> getRound(
            @PathVariable Long id) {

        return agentRoundRepository.findById(id)
                .map(round ->
                        ResponseEntity.ok(
                                AgentRoundResponse.from(round)
                        )
                )
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    @GetMapping("/by-run/{agentRunId}")
    public List<AgentRoundResponse> getRoundsForRun(
            @PathVariable Long agentRunId) {

        return agentRoundRepository
                .findAllByAgentRunId(agentRunId)
                .stream()
                .map(AgentRoundResponse::from)
                .toList();
    }
}