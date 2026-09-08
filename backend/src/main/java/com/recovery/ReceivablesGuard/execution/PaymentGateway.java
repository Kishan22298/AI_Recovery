package com.recovery.ReceivablesGuard.execution;

import java.math.BigDecimal;

public interface PaymentGateway {

    void collectPayment(
            String customerReference,
            String invoiceReference,
            BigDecimal amount
    );
}