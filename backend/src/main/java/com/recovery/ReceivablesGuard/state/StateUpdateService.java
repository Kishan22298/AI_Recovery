package com.recovery.ReceivablesGuard.state;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StateUpdateService {

    private final PaymentStateUpdater paymentStateUpdater;
    private final InvoiceStateUpdater invoiceStateUpdater;

    public StateUpdateService(
            PaymentStateUpdater paymentStateUpdater,
            InvoiceStateUpdater invoiceStateUpdater) {

        this.paymentStateUpdater = paymentStateUpdater;
        this.invoiceStateUpdater = invoiceStateUpdater;
    }

    @Transactional
    public StateUpdateResult applyPayment(
            Long invoiceId,
            BigDecimal outstandingAmount,
            BigDecimal paymentAmount,
            String previousStatus) {

        if (invoiceId == null) {
            throw new IllegalArgumentException(
                    "Invoice ID cannot be null"
            );
        }

        BigDecimal newOutstanding =
                paymentStateUpdater.calculateRemainingOutstanding(
                        outstandingAmount,
                        paymentAmount
                );

        String newStatus =
                invoiceStateUpdater.determineStatus(
                        newOutstanding
                );

        boolean changed =
                !outstandingAmount.equals(newOutstanding)
                        || !previousStatus.equals(newStatus);

        return new StateUpdateResult(
                invoiceId,
                outstandingAmount,
                newOutstanding,
                previousStatus,
                newStatus,
                changed
        );
    }
}