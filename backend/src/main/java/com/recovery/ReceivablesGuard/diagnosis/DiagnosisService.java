package com.recovery.ReceivablesGuard.diagnosis;

import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class DiagnosisService {

    private final DiagnosisClient diagnosisClient;

    private final DiagnosisPromptBuilder promptBuilder;

    private final GeminiResponseParser responseParser;

    private final DiagnosisFallback fallback;

    public DiagnosisService(
            DiagnosisClient diagnosisClient,
            DiagnosisPromptBuilder promptBuilder,
            GeminiResponseParser responseParser,
            DiagnosisFallback fallback
    ) {

        this.diagnosisClient = diagnosisClient;
        this.promptBuilder = promptBuilder;
        this.responseParser = responseParser;
        this.fallback = fallback;
    }

    public DiagnosisResult diagnose(
            Map<String, Object> observation
    ) {

        String prompt =
                promptBuilder.build(observation);

        try {

            String response =
                    diagnosisClient.diagnose(prompt);

            if (response == null
                    || response.isBlank()) {

                return fallback.create(
                        "Gemini returned an empty response"
                );
            }

            return responseParser.parse(response);

        } catch (Exception exception) {

            return fallback.create(
                    exception.getMessage() == null
                            ? "Gemini diagnosis failed"
                            : exception.getMessage()
            );
        }
    }
}