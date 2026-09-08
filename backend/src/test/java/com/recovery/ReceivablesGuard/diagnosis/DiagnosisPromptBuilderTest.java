package com.recovery.ReceivablesGuard.diagnosis;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class DiagnosisPromptBuilderTest {

    private final DiagnosisPromptBuilder builder =
            new DiagnosisPromptBuilder();

    @Test
    void promptShouldContainObservationSignals() {

        String prompt =
                builder.build(
                        Map.of(
                                "outstandingAmount",
                                "15000.00",
                                "invoiceAgeDays",
                                45,
                                "responsiveness",
                                "LOW",
                                "paymentHistory",
                                "Repeated partial payments"
                        )
                );

        assertTrue(
                prompt.contains("15000.00")
        );

        assertTrue(
                prompt.contains("45")
        );

        assertTrue(
                prompt.contains("LOW")
        );

        assertTrue(
                prompt.contains(
                        "Repeated partial payments"
                )
        );
    }

    @Test
    void promptMustExplicitlyRestrictExecution() {

        String prompt =
                builder.build(
                        Map.of(
                                "outstandingAmount",
                                "1000"
                        )
                );

        assertTrue(
                prompt.contains(
                        "execute actions"
                )
        );

        assertTrue(
                prompt.contains(
                        "modify invoices"
                )
        );

        assertTrue(
                prompt.contains(
                        "bypass policy"
                )
        );
    }
}