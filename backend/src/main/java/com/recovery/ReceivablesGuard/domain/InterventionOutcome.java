package com.recovery.ReceivablesGuard.domain;

import java.math.BigDecimal;
import java.time.Instant;

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
import jakarta.persistence.Table;

@Entity
@Table(
        name = "intervention_outcomes",
        indexes = {
            @Index(name = "idx_outcomes_round",
                   columnList = "agent_round_id"),
            @Index(name = "idx_outcomes_invoice",
                   columnList = "invoice_id"),
            @Index(name = "idx_outcomes_type",
                   columnList = "outcome_type")
        })
public class InterventionOutcome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "agent_round_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_outcomes_round"))
    private AgentRound agentRound;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "invoice_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_outcomes_invoice"))
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome_type", nullable = false, length = 40)
    private OutcomeType outcomeType;

    @Column(
            name = "recovered_amount",
            nullable = false,
            precision = 19,
            scale = 4)
    private BigDecimal recoveredAmount = BigDecimal.ZERO;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected InterventionOutcome() {}

    public InterventionOutcome(
            AgentRound agentRound,
            Invoice invoice,
            OutcomeType outcomeType,
            BigDecimal recoveredAmount,
            String notes,
            Instant occurredAt) {

        this.agentRound = agentRound;
        this.invoice = invoice;
        this.outcomeType = outcomeType;
        this.recoveredAmount = recoveredAmount;
        this.notes = notes;
        this.occurredAt = occurredAt;
    }

    public Long getId() {
        return id;
    }

    public AgentRound getAgentRound() {
        return agentRound;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public OutcomeType getOutcomeType() {
        return outcomeType;
    }

    public BigDecimal getRecoveredAmount() {
        return recoveredAmount;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}