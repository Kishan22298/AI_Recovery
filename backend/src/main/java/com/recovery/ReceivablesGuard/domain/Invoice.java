package com.recovery.ReceivablesGuard.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices",
       uniqueConstraints = @UniqueConstraint(
               name = "uk_invoices_external_ref",
               columnNames = "external_ref"),
       indexes = {
           @Index(name = "idx_invoices_customer_status",
                  columnList = "customer_id,status"),
           @Index(name = "idx_invoices_due_date",
                  columnList = "due_date"),
           @Index(name = "idx_invoices_status",
                  columnList = "status")
       })
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_ref", nullable = false, length = 100)
    private String externalRef;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "customer_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_invoices_customer"))
    private Customer customer;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal outstandingAmount;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InvoiceStatus status = InvoiceStatus.OPEN;

    @Column(length = 2000)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "invoice", fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", fetch = FetchType.LAZY)
    private List<Communication> communications = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", fetch = FetchType.LAZY)
    private List<PromiseToPay> promises = new ArrayList<>();

    protected Invoice() {}

    public Invoice(
            String externalRef,
            Customer customer,
            BigDecimal totalAmount,
            String currency,
            LocalDate issueDate,
            LocalDate dueDate) {

        this.externalRef = externalRef;
        this.customer = customer;
        this.totalAmount = totalAmount;
        this.outstandingAmount = totalAmount;
        this.currency = currency;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
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

    public String getExternalRef() {
        return externalRef;
    }

    public Customer getCustomer() {
        return customer;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getOutstandingAmount() {
        return outstandingAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public List<Communication> getCommunications() {
        return communications;
    }

    public List<PromiseToPay> getPromises() {
        return promises;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public void setOutstandingAmount(BigDecimal outstandingAmount) {
        this.outstandingAmount = outstandingAmount;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}