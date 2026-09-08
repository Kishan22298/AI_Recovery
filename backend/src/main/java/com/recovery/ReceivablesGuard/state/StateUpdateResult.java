package com.recovery.ReceivablesGuard.state;

import java.math.BigDecimal;

public record StateUpdateResult(
        Long invoiceId,
        BigDecimal previousOutstandingAmount,
        BigDecimal newOutstandingAmount,
        String previousInvoiceStatus,
        String newInvoiceStatus,
        boolean stateChanged
) {
}