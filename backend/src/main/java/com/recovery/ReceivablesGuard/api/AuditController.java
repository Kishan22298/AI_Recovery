package com.recovery.ReceivablesGuard.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recovery.ReceivablesGuard.api.dto.AuditEventResponse;
import com.recovery.ReceivablesGuard.audit.AuditService;

@RestController
@RequestMapping("/api")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/agent-runs/{agentRunId}/audit")
    public ResponseEntity<List<AuditEventResponse>> getRunAudit(
            @PathVariable Long agentRunId) {

        List<AuditEventResponse> events =
                auditService
                        .reconstructRun(agentRunId)
                        .stream()
                        .map(AuditEventResponse::from)
                        .toList();

        return ResponseEntity.ok(events);
    }

    @GetMapping("/agent-rounds/{agentRoundId}/audit")
    public ResponseEntity<List<AuditEventResponse>> getRoundAudit(
            @PathVariable Long agentRoundId) {

        List<AuditEventResponse> events =
                auditService
                        .reconstructRound(agentRoundId)
                        .stream()
                        .map(AuditEventResponse::from)
                        .toList();

        return ResponseEntity.ok(events);
    }
}