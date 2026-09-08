package com.recovery.ReceivablesGuard.audit;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "audit_events",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_audit_events_run_sequence",
                        columnNames = {"agent_run_id", "sequence_number"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_audit_events_run_sequence",
                        columnList = "agent_run_id, sequence_number"
                ),
                @Index(
                        name = "idx_audit_events_round_sequence",
                        columnList = "agent_round_id, sequence_number"
                ),
                @Index(
                        name = "idx_audit_events_type",
                        columnList = "event_type"
                ),
                @Index(
                        name = "idx_audit_events_created_at",
                        columnList = "created_at"
                )
        }
)
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_run_id", nullable = false)
    private Long agentRunId;

    @Column(name = "agent_round_id")
    private Long agentRoundId;

    @Column(name = "sequence_number", nullable = false)
    private Long sequenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private AuditEventType eventType;

    @Column(name = "actor", nullable = false, length = 50)
    private String actor;

    @Column(name = "event_data", columnDefinition = "json")
    private String eventData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AuditEvent() {
    }

    public AuditEvent(
            Long agentRunId,
            Long agentRoundId,
            Long sequenceNumber,
            AuditEventType eventType,
            String actor,
            String eventData
    ) {
        this.agentRunId = agentRunId;
        this.agentRoundId = agentRoundId;
        this.sequenceNumber = sequenceNumber;
        this.eventType = eventType;
        this.actor = actor;
        this.eventData = eventData;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getAgentRunId() {
        return agentRunId;
    }

    public Long getAgentRoundId() {
        return agentRoundId;
    }

    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    public AuditEventType getEventType() {
        return eventType;
    }

    public String getActor() {
        return actor;
    }

    public String getEventData() {
        return eventData;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}