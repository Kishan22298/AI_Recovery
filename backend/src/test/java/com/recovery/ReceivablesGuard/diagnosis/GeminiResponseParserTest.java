package com.recovery.ReceivablesGuard.diagnosis;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class GeminiResponseParserTest {

    private final GeminiResponseParser parser =
            new GeminiResponseParser(
                    new ObjectMapper()
            );

    @Test
    void validResponseShouldParse() {

        String response = """
                {
                  "category": "CASH_FLOW_ISSUE",
                  "explanation": "Customer appears to have a cash flow problem.",
                  "evidence": [
                    {
                      "signal": "payment_history",
                      "observation": "Recent partial payments",
                      "source": "observation"
                    }
                  ]
                }
                """;

        DiagnosisResult result =
                parser.parse(response);

        assertEquals(
                DiagnosisCategory.CASH_FLOW_ISSUE,
                result.category()
        );

        assertFalse(result.fallback());

        assertEquals(
                1,
                result.evidence().size()
        );
    }

    @Test
    void malformedJsonShouldFail() {

        String response = """
                {"category":"CASH_FLOW_ISSUE"
                """;

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(response)
        );
    }

    @Test
    void invalidCategoryShouldFail() {

        String response = """
                {
                  "category": "SEND_EMAIL",
                  "explanation": "Bad category",
                  "evidence": []
                }
                """;

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(response)
        );
    }

    @Test
    void missingCategoryShouldFail() {

        String response = """
                {
                  "explanation": "Missing category",
                  "evidence": []
                }
                """;

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(response)
        );
    }

    @Test
    void missingExplanationShouldFail() {

        String response = """
                {
                  "category": "DISPUTE",
                  "evidence": []
                }
                """;

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(response)
        );
    }

    @Test
    void missingEvidenceShouldFail() {

        String response = """
                {
                  "category": "DISPUTE",
                  "explanation": "Customer disputes invoice"
                }
                """;

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(response)
        );
    }

    @Test
    void emptyResponseShouldFail() {

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse("")
        );
    }

    @Test
    void markdownJsonShouldBeAccepted() {

        String response = """
                ```json
                {
                  "category": "DISPUTE",
                  "explanation": "Customer disputes the invoice.",
                  "evidence": []
                }
                ```
                """;

        DiagnosisResult result =
                parser.parse(response);

        assertEquals(
                DiagnosisCategory.DISPUTE,
                result.category()
        );
    }
}