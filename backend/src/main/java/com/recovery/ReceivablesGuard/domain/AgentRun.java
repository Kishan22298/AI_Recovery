package com.recovery.ReceivablesGuard.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "agent_runs",
       indexes = {
           @Index(name = "idx_agent_runs_invoice", columnList = "invoice_id"),
           @Index(name = "idx_agent_runs_status", columnList = "status"),
           @Index(name = "idx_agent_runs_started_at", columnList = "started_at")
       })
public class AgentRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "invoice_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_agent_runs_invoice"))
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AgentRunStatus status = AgentRunStatus.CREATED;

    @Column(nullable = false)
    private Integer maxRounds = 3;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "agentRun", fetch = FetchType.LAZY)
    @OrderBy("roundNumber ASC")
    private List<AgentRound> rounds = new ArrayList<>();

    protected AgentRun() {}

    public AgentRun(Invoice invoice, int maxRounds) {
        this.invoice = invoice;
        this.maxRounds = maxRounds;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public AgentRunStatus getStatus() {
        return status;
    }

    public Integer getMaxRounds() {
        return maxRounds;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<AgentRound> getRounds() {
        return rounds;
    }

    public void setStatus(AgentRunStatus status) {
        this.status = status;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}