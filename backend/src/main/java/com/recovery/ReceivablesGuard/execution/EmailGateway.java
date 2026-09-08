package com.recovery.ReceivablesGuard.execution;

public interface EmailGateway {

    void send(
            String customerReference,
            String invoiceReference,
            String message
    );
}