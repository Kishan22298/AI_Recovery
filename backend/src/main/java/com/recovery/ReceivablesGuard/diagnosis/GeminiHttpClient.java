package com.recovery.ReceivablesGuard.diagnosis;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GeminiHttpClient {

    private final RestClient restClient;

    private final String apiKey;

    private final String model;

    public GeminiHttpClient(
            RestClient.Builder restClientBuilder,
            @Value("${gemini.api-key:}") String apiKey,
            @Value("${gemini.model:gemini-2.5-flash}") String model
    ) {

        this.restClient =
                restClientBuilder
                        .baseUrl(
                                "https://generativelanguage.googleapis.com"
                        )
                        .build();

        this.apiKey = apiKey;
        this.model = model;
    }

    public String generate(String prompt) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new GeminiUnavailableException(
                    "Gemini API key is not configured"
            );
        }

        GeminiRequest request =
                GeminiRequest.from(prompt);

        GeminiResponse response =
                restClient.post()
                        .uri(uriBuilder ->
                                uriBuilder
                                        .path(
                                            "/v1beta/models/"
                                        )
                                        .path(model)
                                        .path(":generateContent")
                                        .queryParam(
                                            "key",
                                            apiKey
                                        )
                                        .build()
                        )
                        .body(request)
                        .retrieve()
                        .body(GeminiResponse.class);

        if (response == null) {
            throw new GeminiUnavailableException(
                    "Empty Gemini response"
            );
        }

        return response.extractText();
    }
}