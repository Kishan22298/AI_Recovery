package com.recovery.ReceivablesGuard.diagnosis;

public record DiagnosisEvidence(
        String signal,
        String observation,
        String source
) {

    public DiagnosisEvidence {
        if (signal == null || signal.isBlank()) {
            throw new IllegalArgumentException("signal must not be blank");
        }

        if (observation == null || observation.isBlank()) {
            throw new IllegalArgumentException("observation must not be blank");
        }

        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("source must not be blank");
        }
    }
}