package com.recovery.ReceivablesGuard.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.recovery.ReceivablesGuard.domain.Payment;

public record PaymentResponse(
        Long id,
        BigDecimal amount,
        String currency,
        LocalDateTime receivedAt,
        String status,
        String reference
) {

    public static PaymentResponse from(Payment payment) {

        if (payment == null) {
            throw new IllegalArgumentException(
                    "Payment must not be null"
            );
        }

        return new PaymentResponse(
                payment.getId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getReceivedAt(),
                payment.getStatus() != null
                        ? payment.getStatus().name()
                        : null,
                payment.getReference()
        );
    }
}