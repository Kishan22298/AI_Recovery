package com.recovery.ReceivablesGuard.agent;

public record BatchRunItem(
        String invoiceReference,
        String status,
        AgentRunResult result,
        Throwable error) {

    public static BatchRunItem success(
            String invoiceReference,
            AgentRunResult result) {

        return new BatchRunItem(
                invoiceReference,
                "COMPLETED",
                result,
                null);
    }

    public static BatchRunItem failure(
            String invoiceReference,
            Throwable error) {

        return new BatchRunItem(
                invoiceReference,
                "FAILED",
                null,
                error);
    }
}