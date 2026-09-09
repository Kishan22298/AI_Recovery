package com.recovery.ReceivablesGuard.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "promises_to_pay",
       indexes = {
           @Index(name = "idx_ptp_invoice_status",
                  columnList = "invoice_id,status"),
           @Index(name = "idx_ptp_promised_date",
                  columnList = "promised_date")
       })
public class PromiseToPay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "invoice_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_ptp_invoice"))
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "customer_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_ptp_customer"))
    private Customer customer;

    @Column(
            name = "promised_amount",
            nullable = false,
            precision = 19,
            scale = 4)
    private BigDecimal promisedAmount;

    @Column(name = "promised_date", nullable = false)
    private LocalDate promisedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PromiseStatus status = PromiseStatus.OPEN;

    @Column(name = "broken_at")
    private Instant brokenAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PromiseToPay() {}

    public PromiseToPay(
            Invoice invoice,
            Customer customer,
            BigDecimal promisedAmount,
            LocalDate promisedDate) {

        this.invoice = invoice;
        this.customer = customer;
        this.promisedAmount = promisedAmount;
        this.promisedDate = promisedDate;
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

    public Customer getCustomer() {
        return customer;
    }

    public BigDecimal getPromisedAmount() {
        return promisedAmount;
    }

    public LocalDate getPromisedDate() {
        return promisedDate;
    }

    public PromiseStatus getStatus() {
        return status;
    }

    public Instant getBrokenAt() {
        return brokenAt;
    }
public Instant getCreatedAt() {
    return createdAt;
}

    public void setStatus(PromiseStatus status) {
        this.status = status;
    }

    public void setBrokenAt(Instant brokenAt) {
        this.brokenAt = brokenAt;
    }
}