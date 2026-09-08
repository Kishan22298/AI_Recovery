package com.recovery.ReceivablesGuard.diagnosis;

import java.util.List;

public record GeminiResponse(
        List<Candidate> candidates
) {

    public String extractText() {

        if (candidates == null
                || candidates.isEmpty()) {

            throw new GeminiUnavailableException(
                    "Gemini returned no candidates"
            );
        }

        Candidate candidate = candidates.get(0);

        if (candidate == null
                || candidate.content() == null
                || candidate.content().parts() == null
                || candidate.content().parts().isEmpty()) {

            throw new GeminiUnavailableException(
                    "Gemini response contained no text"
            );
        }

        return candidate
                .content()
                .parts()
                .get(0)
                .text();
    }

    public record Candidate(
            Content content
    ) {}

    public record Content(
            List<Part> parts
    ) {}

    public record Part(
            String text
    ) {}
}