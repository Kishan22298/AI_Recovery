package com.recovery.ReceivablesGuard.diagnosis;

import java.util.List;

public record GeminiRequest(
        List<Content> contents
) {

    public static GeminiRequest from(String prompt) {

        return new GeminiRequest(
                List.of(
                        new Content(
                                List.of(
                                        new Part(prompt)
                                )
                        )
                )
        );
    }

    public record Content(
            List<Part> parts
    ) {}

    public record Part(
            String text
    ) {}
}