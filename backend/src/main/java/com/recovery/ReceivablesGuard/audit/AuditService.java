package com.recovery.ReceivablesGuard.audit;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private static final Set<String> FORBIDDEN_KEYS = Set.of(
            "password",
            "passwd",
            "secret",
            "api_key",
            "apikey",
            "access_token",
            "refresh_token",
            "authorization",
            "client_secret"
    );

    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    /**
     * Append one immutable audit event.
     *
     * Events are never updated through this service.
     */
    @Transactional
    public AuditEvent append(
            Long agentRunId,
            Long agentRoundId,
            AuditEventType eventType,
            String actor,
            String eventData
    ) {
        validate(agentRunId, eventType, actor, eventData);

        long nextSequence =
                auditEventRepository.countByAgentRunId(agentRunId) + 1L;

        AuditEvent event = new AuditEvent(
                agentRunId,
                agentRoundId,
                nextSequence,
                eventType,
                actor,
                eventData
        );

        return auditEventRepository.save(event);
    }

    /**
     * Reconstruct complete agent run.
     */
    @Transactional(readOnly = true)
    public List<AuditEvent> reconstructRun(Long agentRunId) {

        if (agentRunId == null) {
            throw new IllegalArgumentException(
                    "agentRunId must not be null"
            );
        }

        return auditEventRepository
                .findByAgentRunIdOrderBySequenceNumberAsc(agentRunId);
    }

    /**
     * Reconstruct one agent round.
     */
    @Transactional(readOnly = true)
    public List<AuditEvent> reconstructRound(Long agentRoundId) {

        if (agentRoundId == null) {
            throw new IllegalArgumentException(
                    "agentRoundId must not be null"
            );
        }

        return auditEventRepository
                .findByAgentRoundIdOrderBySequenceNumberAsc(agentRoundId);
    }

    /**
     * Check whether an event type exists in a reconstructed lifecycle.
     *
     * This is useful for detecting missing lifecycle events.
     */
    public boolean containsEventType(
            List<AuditEvent> events,
            AuditEventType eventType
    ) {
        if (events == null || eventType == null) {
            return false;
        }

        return events.stream()
                .anyMatch(event -> event.getEventType() == eventType);
    }

    /**
     * Validate lifecycle completeness.
     *
     * Does not modify audit data.
     */
    public List<AuditEventType> missingLifecycleEvents(
            List<AuditEvent> events
    ) {
        return java.util.Arrays.stream(AuditEventType.values())
                .filter(type -> !containsEventType(events, type))
                .toList();
    }

    private void validate(
            Long agentRunId,
            AuditEventType eventType,
            String actor,
            String eventData
    ) {
        if (agentRunId == null) {
            throw new IllegalArgumentException(
                    "agentRunId must not be null"
            );
        }

        if (eventType == null) {
            throw new IllegalArgumentException(
                    "eventType must not be null"
            );
        }

        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException(
                    "actor must not be blank"
            );
        }

        if (actor.length() > 50) {
            throw new IllegalArgumentException(
                    "actor must not exceed 50 characters"
            );
        }

        if (eventData != null && containsSecretKey(eventData)) {
            throw new IllegalArgumentException(
                    "Audit event contains a forbidden secret field"
            );
        }
    }

    private boolean containsSecretKey(String eventData) {

        String normalized = eventData.toLowerCase();

        return FORBIDDEN_KEYS.stream()
                .anyMatch(normalized::contains);
    }
}