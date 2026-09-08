package com.recovery.ReceivablesGuard.diagnosis;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class GeminiResponseParser {

    private final ObjectMapper objectMapper;

    public GeminiResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public DiagnosisResult parse(String response) {

        if (response == null || response.isBlank()) {
            throw new IllegalArgumentException(
                    "Gemini response is empty"
            );
        }

        try {

            String json = cleanJson(response);

            JsonNode root = objectMapper.readTree(json);

            validateRequiredFields(root);

            String categoryText =
                    root.get("category").asText();

            DiagnosisCategory category;

            try {
                category = DiagnosisCategory.valueOf(
                        categoryText.trim().toUpperCase()
                );
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException(
                        "Invalid diagnosis category: "
                                + categoryText
                );
            }

            String explanation =
                    root.get("explanation").asText();

            if (explanation.isBlank()) {
                throw new IllegalArgumentException(
                        "Missing explanation"
                );
            }

            List<DiagnosisEvidence> evidence =
                    parseEvidence(root.get("evidence"));

            return new DiagnosisResult(
                    category,
                    explanation,
                    evidence,
                    false,
                    "gemini"
            );

        } catch (Exception exception) {

            if (exception instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) exception;
            }

            throw new IllegalArgumentException(
                    "Malformed Gemini JSON response",
                    exception
            );
        }
    }

    private void validateRequiredFields(JsonNode root) {

        if (root == null || !root.isObject()) {
            throw new IllegalArgumentException(
                    "Gemini response must be a JSON object"
            );
        }

        if (!root.hasNonNull("category")) {
            throw new IllegalArgumentException(
                    "Missing category"
            );
        }

        if (!root.hasNonNull("explanation")) {
            throw new IllegalArgumentException(
                    "Missing explanation"
            );
        }

        if (!root.has("evidence")) {
            throw new IllegalArgumentException(
                    "Missing evidence"
            );
        }

        if (!root.get("evidence").isArray()) {
            throw new IllegalArgumentException(
                    "Evidence must be an array"
            );
        }
    }

    private List<DiagnosisEvidence> parseEvidence(
            JsonNode evidenceNode
    ) {

        List<DiagnosisEvidence> evidence =
                new ArrayList<>();

        for (JsonNode item : evidenceNode) {

            if (!item.hasNonNull("signal")
                    || !item.hasNonNull("observation")
                    || !item.hasNonNull("source")) {

                throw new IllegalArgumentException(
                        "Invalid evidence item"
                );
            }

            evidence.add(
                    new DiagnosisEvidence(
                            item.get("signal").asText(),
                            item.get("observation").asText(),
                            item.get("source").asText()
                    )
            );
        }

        return evidence;
    }

    private String cleanJson(String response) {

        String cleaned = response.trim();

        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(
                    0,
                    cleaned.length() - 3
            );
        }

        return cleaned.trim();
    }
}