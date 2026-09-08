package com.recovery.ReceivablesGuard.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recovery.ReceivablesGuard.api.dto.AuditEventResponse;
import com.recovery.ReceivablesGuard.audit.AuditEventType;
import com.recovery.ReceivablesGuard.audit.AuditService;

@RestController
@RequestMapping("/api")
public class DecisionController {

    private final AuditService auditService;

    public DecisionController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/agent-runs/{agentRunId}/decisions")
    public ResponseEntity<List<AuditEventResponse>> getDecisions(
            @PathVariable Long agentRunId) {

        List<AuditEventResponse> decisions =
                auditService
                        .reconstructRun(agentRunId)
                        .stream()
                        .filter(event ->
                                isDecisionEvent(
                                        event.getEventType()
                                )
                        )
                        .map(AuditEventResponse::from)
                        .toList();

        return ResponseEntity.ok(decisions);
    }

    private boolean isDecisionEvent(
            AuditEventType eventType) {

        return eventType == AuditEventType.CANDIDATE_STRATEGIES
                || eventType == AuditEventType.PROPENSITY
                || eventType == AuditEventType.EXPECTED_VALUE
                || eventType == AuditEventType.RANKING
                || eventType == AuditEventType.POLICY
                || eventType == AuditEventType.FINAL_DECISION;
    }
}