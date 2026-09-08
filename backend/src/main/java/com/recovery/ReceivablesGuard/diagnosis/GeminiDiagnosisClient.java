package com.recovery.ReceivablesGuard.diagnosis;

import org.springframework.stereotype.Component;

@Component
public class GeminiDiagnosisClient implements DiagnosisClient {

    private final GeminiHttpClient httpClient;

    public GeminiDiagnosisClient(
            GeminiHttpClient httpClient
    ) {
        this.httpClient = httpClient;
    }

    @Override
    public String diagnose(String prompt) {

        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException(
                    "Prompt must not be blank"
            );
        }

        return httpClient.generate(prompt);
    }
}