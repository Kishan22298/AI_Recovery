package com.recovery.ReceivablesGuard.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.recovery.ReceivablesGuard.domain.Invoice;

public record InvoiceResponse(
        Long id,
        String externalRef,
        Long customerId,
String customerReference,
String customerName,
String customerEmail,
String customerPhone,
        BigDecimal totalAmount,
        BigDecimal outstandingAmount,
        String currency,
        LocalDate issueDate,
        LocalDate dueDate,
        String status,
        String description
) {

    public static InvoiceResponse from(Invoice invoice) {

        if (invoice == null) {
            throw new IllegalArgumentException(
                    "Invoice must not be null"
            );
        }

        return new InvoiceResponse(
                invoice.getId(),
                invoice.getExternalRef(),
                invoice.getCustomer() != null
                        ? invoice.getCustomer().getId()
                        : null,
                invoice.getCustomer() != null
        ? invoice.getCustomer().getExternalRef()
        : null,
invoice.getCustomer() != null
        ? invoice.getCustomer().getName()
        : null,
invoice.getCustomer() != null
        ? invoice.getCustomer().getEmail()
        : null,
invoice.getCustomer() != null
        ? invoice.getCustomer().getPhone()
        : null,
                invoice.getTotalAmount(),
                invoice.getOutstandingAmount(),
                invoice.getCurrency(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getStatus() != null
                        ? invoice.getStatus().name()
                        : null,
                invoice.getDescription()
        );
    }
}