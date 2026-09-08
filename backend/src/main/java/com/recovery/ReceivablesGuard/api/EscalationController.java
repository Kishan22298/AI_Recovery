package com.recovery.ReceivablesGuard.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recovery.ReceivablesGuard.api.dto.EscalationResponse;
import com.recovery.ReceivablesGuard.audit.AuditEventRepository;
import com.recovery.ReceivablesGuard.audit.AuditEventType;

@RestController
@RequestMapping("/api/escalations")
public class EscalationController {

    private final AuditEventRepository auditEventRepository;

    public EscalationController(
            AuditEventRepository auditEventRepository) {

        this.auditEventRepository =
                auditEventRepository;
    }

    @GetMapping
    public List<EscalationResponse> getEscalations() {

        return auditEventRepository
                .findAllByEventType(
                        AuditEventType.POLICY
                )
                .stream()
                .filter(event ->
                        event.getEventData() != null
                                && event.getEventData()
                                .contains(
                                        "\"decision\":\"HUMAN_ESCALATION\""
                                )
                )
                .map(EscalationResponse::from)
                .toList();
    }
}