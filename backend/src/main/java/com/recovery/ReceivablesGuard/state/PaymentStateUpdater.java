package com.recovery.ReceivablesGuard.state;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

@Component
public class PaymentStateUpdater {

    public BigDecimal calculateRemainingOutstanding(
            BigDecimal outstandingAmount,
            BigDecimal paymentAmount) {

        if (outstandingAmount == null) {
            throw new IllegalArgumentException(
                    "Outstanding amount cannot be null"
            );
        }

        if (paymentAmount == null) {
            throw new IllegalArgumentException(
                    "Payment amount cannot be null"
            );
        }

        if (paymentAmount.signum() < 0) {
            throw new IllegalArgumentException(
                    "Payment amount cannot be negative"
            );
        }

        if (paymentAmount.compareTo(outstandingAmount) > 0) {
            throw new IllegalArgumentException(
                    "Payment cannot exceed outstanding amount"
            );
        }

        return outstandingAmount.subtract(paymentAmount);
    }
}