package com.recovery.ReceivablesGuard.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "guardrail_configs",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_guardrail_configs_name",
                columnNames = "config_name"))
public class GuardrailConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_name", nullable = false, length = 100)
    private String configName;

    @Column(name = "contact_cap_per_week", nullable = false)
    private Integer contactCapPerWeek = 3;

    @Column(name = "cooldown_hours", nullable = false)
    private Integer cooldownHours = 24;

    @Column(
            name = "discount_ceiling_percent",
            nullable = false,
            precision = 5,
            scale = 2)
    private BigDecimal discountCeilingPercent = BigDecimal.TEN;

    @Column(
            name = "monetary_escalation_threshold",
            precision = 19,
            scale = 4)
    private BigDecimal monetaryEscalationThreshold;

    @Column(name = "business_hours_start", nullable = false)
    private LocalTime businessHoursStart = LocalTime.of(9, 0);

    @Column(name = "business_hours_end", nullable = false)
    private LocalTime businessHoursEnd = LocalTime.of(18, 0);

    @Column(name = "broken_promise_lockout_hours", nullable = false)
    private Integer brokenPromiseLockoutHours = 72;

    @Column(name = "circuit_breaker_enabled", nullable = false)
    private boolean circuitBreakerEnabled = true;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected GuardrailConfig() {}

    public GuardrailConfig(String configName) {
        this.configName = configName;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getConfigName() {
        return configName;
    }

    public Integer getContactCapPerWeek() {
        return contactCapPerWeek;
    }

    public Integer getCooldownHours() {
        return cooldownHours;
    }

    public BigDecimal getDiscountCeilingPercent() {
        return discountCeilingPercent;
    }

    public BigDecimal getMonetaryEscalationThreshold() {
        return monetaryEscalationThreshold;
    }

    public LocalTime getBusinessHoursStart() {
        return businessHoursStart;
    }

    public LocalTime getBusinessHoursEnd() {
        return businessHoursEnd;
    }

    public Integer getBrokenPromiseLockoutHours() {
        return brokenPromiseLockoutHours;
    }

    public boolean isCircuitBreakerEnabled() {
        return circuitBreakerEnabled;
    }

    public boolean isActive() {
        return active;
    }
}