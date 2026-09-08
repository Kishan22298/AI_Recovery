package com.recovery.ReceivablesGuard.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers",
       uniqueConstraints = @UniqueConstraint(
               name = "uk_customers_external_ref",
               columnNames = "external_ref"),
       indexes = {
           @Index(name = "idx_customers_email", columnList = "email"),
           @Index(name = "idx_customers_status", columnList = "status")
       })
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_ref", nullable = false, length = 100)
    private String externalRef;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 320)
    private String email;

    @Column(length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CustomerStatus status = CustomerStatus.ACTIVE;

    @Column(name = "do_not_contact", nullable = false)
    private boolean doNotContact = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<Invoice> invoices = new ArrayList<>();

    protected Customer() {}

    public Customer(String externalRef, String name, String email, String phone) {
        this.externalRef = externalRef;
        this.name = name;
        this.email = email;
        this.phone = phone;
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

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public boolean isDoNotContact() {
        return doNotContact;
    }

    public List<Invoice> getInvoices() {
        return invoices;
    }

    public void setStatus(CustomerStatus status) {
        this.status = status;
    }

    public void setDoNotContact(boolean doNotContact) {
        this.doNotContact = doNotContact;
    }
}