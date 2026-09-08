package com.recovery.ReceivablesGuard.agent;

import java.util.List;

public record BatchRunResult(
        List<BatchRunItem> results) {
}