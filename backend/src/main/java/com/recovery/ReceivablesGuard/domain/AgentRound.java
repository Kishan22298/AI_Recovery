package com.recovery.ReceivablesGuard.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "agent_rounds",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_agent_rounds_run_number",
                columnNames = {"agent_run_id", "round_number"}),
        indexes = {
            @Index(name = "idx_agent_rounds_run",
                   columnList = "agent_run_id"),
            @Index(name = "idx_agent_rounds_status",
                   columnList = "status")
        })
public class AgentRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "agent_run_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_agent_rounds_run"))
    private AgentRun agentRun;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AgentRoundStatus status = AgentRoundStatus.CREATED;

    @Column(name = "diagnosis_category", length = 50)
    private String diagnosisCategory;

    @Column(name = "propensity_score", precision = 7, scale = 6)
    private java.math.BigDecimal propensityScore;

    @Column(name = "selected_strategy", length = 50)
    private String selectedStrategy;

    @Column(name = "authorized", nullable = false)
    private boolean authorized = false;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @OneToMany(mappedBy = "agentRound", fetch = FetchType.LAZY)
    private List<InterventionOutcome> outcomes = new ArrayList<>();

    protected AgentRound() {}

    public AgentRound(AgentRun agentRun, int roundNumber) {
        this.agentRun = agentRun;
        this.roundNumber = roundNumber;
    }

    public Long getId() {
        return id;
    }

    public AgentRun getAgentRun() {
        return agentRun;
    }

    public Integer getRoundNumber() {
        return roundNumber;
    }

    public AgentRoundStatus getStatus() {
        return status;
    }

    public String getDiagnosisCategory() {
        return diagnosisCategory;
    }

    public java.math.BigDecimal getPropensityScore() {
        return propensityScore;
    }

    public String getSelectedStrategy() {
        return selectedStrategy;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public List<InterventionOutcome> getOutcomes() {
        return outcomes;
    }

    public void setStatus(AgentRoundStatus status) {
        this.status = status;
    }

    public void setDiagnosisCategory(String diagnosisCategory) {
        this.diagnosisCategory = diagnosisCategory;
    }

    public void setPropensityScore(java.math.BigDecimal propensityScore) {
        this.propensityScore = propensityScore;
    }

    public void setSelectedStrategy(String selectedStrategy) {
        this.selectedStrategy = selectedStrategy;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}