package com.recovery.ReceivablesGuard.diagnosis;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

class DiagnosisServiceTest {

    @Test
    void validGeminiResponseShouldProduceDiagnosis() {

        DiagnosisClient client =
                prompt -> """
                        {
                          "category": "PROMISE_TO_PAY",
                          "explanation": "Customer promised payment.",
                          "evidence": [
                            {
                              "signal": "promise",
                              "observation": "Payment promised",
                              "source": "observation"
                            }
                          ]
                        }
                        """;

        DiagnosisService service =
                createService(client);

        DiagnosisResult result =
                service.diagnose(
                        Map.of(
                                "outstandingAmount",
                                "1000.00",
                                "invoiceAgeDays",
                                30,
                                "responsiveness",
                                "HIGH"
                        )
                );

        assertEquals(
                DiagnosisCategory.PROMISE_TO_PAY,
                result.category()
        );

        assertFalse(result.fallback());
    }

    @Test
    void malformedResponseShouldFallback() {

        DiagnosisClient client =
                prompt -> "{invalid json";

        DiagnosisService service =
                createService(client);

        DiagnosisResult result =
                service.diagnose(
                        Map.of(
                                "outstandingAmount",
                                "1000.00"
                        )
                );

        assertEquals(
                DiagnosisCategory.UNKNOWN,
                result.category()
        );

        assertTrue(result.fallback());
    }

    @Test
    void invalidCategoryShouldFallback() {

        DiagnosisClient client =
                prompt -> """
                        {
                          "category": "EXECUTE_PAYMENT",
                          "explanation": "Invalid",
                          "evidence": []
                        }
                        """;

        DiagnosisService service =
                createService(client);

        DiagnosisResult result =
                service.diagnose(
                        Map.of(
                                "outstandingAmount",
                                "500"
                        )
                );

        assertEquals(
                DiagnosisCategory.UNKNOWN,
                result.category()
        );

        assertTrue(result.fallback());
    }

    @Test
    void emptyResponseShouldFallback() {

        DiagnosisClient client =
                prompt -> "";

        DiagnosisService service =
                createService(client);

        DiagnosisResult result =
                service.diagnose(
                        Map.of(
                                "outstandingAmount",
                                "500"
                        )
                );

        assertTrue(result.fallback());
    }

    @Test
    void timeoutShouldFallback() {

        DiagnosisClient client =
                prompt -> {
                    throw new RuntimeException(
                            "timeout"
                    );
                };

        DiagnosisService service =
                createService(client);

        DiagnosisResult result =
                service.diagnose(
                        Map.of(
                                "outstandingAmount",
                                "500"
                        )
                );

        assertEquals(
                DiagnosisCategory.UNKNOWN,
                result.category()
        );

        assertTrue(result.fallback());
    }

    @Test
    void apiFailureShouldFallback() {

        DiagnosisClient client =
                prompt -> {
                    throw new GeminiUnavailableException(
                            "API unavailable"
                    );
                };

        DiagnosisService service =
                createService(client);

        DiagnosisResult result =
                service.diagnose(
                        Map.of(
                                "outstandingAmount",
                                "500"
                        )
                );

        assertTrue(result.fallback());
    }

    private DiagnosisService createService(
            DiagnosisClient client
    ) {

        DiagnosisPromptBuilder promptBuilder =
                new DiagnosisPromptBuilder();

        GeminiResponseParser parser =
                new GeminiResponseParser(
                        new com.fasterxml.jackson.databind.ObjectMapper()
                );

        DiagnosisFallback fallback =
                new DiagnosisFallback();

        return new DiagnosisService(
                client,
                promptBuilder,
                parser,
                fallback
        );
    }
}