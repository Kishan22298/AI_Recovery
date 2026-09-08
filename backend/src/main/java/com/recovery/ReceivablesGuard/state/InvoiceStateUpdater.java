package com.recovery.ReceivablesGuard.state;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

@Component
public class InvoiceStateUpdater {

    public String determineStatus(BigDecimal outstandingAmount) {

        if (outstandingAmount == null) {
            throw new IllegalArgumentException(
                    "Outstanding amount cannot be null"
            );
        }

        if (outstandingAmount.signum() < 0) {
            throw new IllegalArgumentException(
                    "Outstanding amount cannot be negative"
            );
        }

        if (outstandingAmount.signum() == 0) {
            return "PAID";
        }

        return "OUTSTANDING";
    }
}