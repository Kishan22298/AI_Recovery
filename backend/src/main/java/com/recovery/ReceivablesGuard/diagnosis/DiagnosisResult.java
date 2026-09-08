package com.recovery.ReceivablesGuard.diagnosis;

import java.util.List;

public record DiagnosisResult(

        DiagnosisCategory category,

        String explanation,

        List<DiagnosisEvidence> evidence,

        boolean fallback,

        String model
) {

    public DiagnosisResult {
        if (category == null) {
            throw new IllegalArgumentException("category must not be null");
        }

        if (explanation == null || explanation.isBlank()) {
            throw new IllegalArgumentException("explanation must not be blank");
        }

        evidence = evidence == null
                ? List.of()
                : List.copyOf(evidence);

        if (model == null || model.isBlank()) {
            model = "unknown";
        }
    }

    public static DiagnosisResult fallback(
            DiagnosisCategory category,
            String explanation,
            List<DiagnosisEvidence> evidence
    ) {
        return new DiagnosisResult(
                category,
                explanation,
                evidence,
                true,
                "fallback"
        );
    }
}