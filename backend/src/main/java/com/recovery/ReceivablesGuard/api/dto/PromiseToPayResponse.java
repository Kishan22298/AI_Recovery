package com.recovery.ReceivablesGuard.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.recovery.ReceivablesGuard.domain.PromiseToPay;

public record PromiseToPayResponse(
        Long id,
        BigDecimal promisedAmount,
        LocalDate promisedDate,
        String status,
        Instant brokenAt,
        Instant createdAt
) {

    public static PromiseToPayResponse from(
            PromiseToPay promise) {

        if (promise == null) {
            throw new IllegalArgumentException(
                    "Promise to pay must not be null"
            );
        }

        return new PromiseToPayResponse(
                promise.getId(),
                promise.getPromisedAmount(),
                promise.getPromisedDate(),
                promise.getStatus() != null
                        ? promise.getStatus().name()
                        : null,
                promise.getBrokenAt(),
                promise.getCreatedAt()
        );
    }
}