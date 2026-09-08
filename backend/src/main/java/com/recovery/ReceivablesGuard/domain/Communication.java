package com.recovery.ReceivablesGuard.domain;

import java.time.Instant;
import java.time.LocalDateTime;

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
@Table(name = "communications",
       indexes = {
           @Index(
               name = "idx_communications_customer_time",
               columnList = "customer_id,sent_at"),
           @Index(
               name = "idx_communications_invoice_time",
               columnList = "invoice_id,sent_at"),
           @Index(
               name = "idx_communications_channel",
               columnList = "channel")
       })
public class Communication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "customer_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_communications_customer"))
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "invoice_id",
            foreignKey = @ForeignKey(name = "fk_communications_invoice"))
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommunicationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommunicationDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CommunicationStatus status;

    @Column(length = 100)
    private String subject;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "template_key", length = 100)
    private String templateKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Communication() {}

    public Communication(
            Customer customer,
            Invoice invoice,
            CommunicationChannel channel,
            CommunicationDirection direction,
            CommunicationStatus status,
            String subject,
            LocalDateTime sentAt,
            String templateKey) {

        this.customer = customer;
        this.invoice = invoice;
        this.channel = channel;
        this.direction = direction;
        this.status = status;
        this.subject = subject;
        this.sentAt = sentAt;
        this.templateKey = templateKey;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public CommunicationChannel getChannel() {
        return channel;
    }

    public CommunicationDirection getDirection() {
        return direction;
    }

    public CommunicationStatus getStatus() {
        return status;
    }

    public String getSubject() {
        return subject;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public String getTemplateKey() {
        return templateKey;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }
}