package com.recovery.ReceivablesGuard.diagnosis;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class DiagnosisFallback {

    public DiagnosisResult create(String reason) {

        return DiagnosisResult.fallback(
                DiagnosisCategory.UNKNOWN,
                "Diagnosis unavailable. "
                        + "Deterministic fallback used. "
                        + reason,
                List.of()
        );
    }
}