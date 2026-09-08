package com.recovery.ReceivablesGuard.diagnosis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class DiagnosisFallbackTest {

    private final DiagnosisFallback fallback =
            new DiagnosisFallback();

    @Test
    void fallbackShouldProduceUnknownDiagnosis() {

        DiagnosisResult result =
                fallback.create("Gemini timeout");

        assertEquals(
                DiagnosisCategory.UNKNOWN,
                result.category()
        );

        assertTrue(result.fallback());

        assertEquals(
                "fallback",
                result.model()
        );
    }
}