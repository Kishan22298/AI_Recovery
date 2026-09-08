package com.recovery.ReceivablesGuard.audit;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository
        extends JpaRepository<AuditEvent, Long> {

    List<AuditEvent> findByAgentRunIdOrderBySequenceNumberAsc(
            Long agentRunId
    );

    List<AuditEvent> findByAgentRoundIdOrderBySequenceNumberAsc(
            Long agentRoundId
    );

    long countByAgentRunId(Long agentRunId);

    List<AuditEvent> findAllByEventType(
            AuditEventType eventType
    );
}