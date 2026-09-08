package com.recovery.ReceivablesGuard.diagnosis;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class DiagnosisPromptBuilder {

    public String build(Map<String, Object> observation) {

        if (observation == null) {
            throw new IllegalArgumentException(
                    "observation must not be null"
            );
        }

        StringBuilder prompt = new StringBuilder();

        prompt.append("""
                You are a receivables diagnosis assistant.

                Your ONLY responsibility is diagnosis.

                You MUST NOT:
                - execute actions
                - send emails
                - send messages
                - make payments
                - modify invoices
                - modify customer state
                - bypass policy
                - calculate recovery decisions
                - rank interventions
                - call external services

                Analyze the supplied observation and identify the most
                likely receivables situation.

                Allowed diagnosis categories:

                CASH_FLOW_ISSUE
                DISPUTE
                PROMISE_TO_PAY
                BROKEN_PROMISE
                UNRESPONSIVE
                PARTIAL_PAYMENT
                PAYMENT_LIKELY
                ESCALATION_REQUIRED
                UNKNOWN

                Return ONLY valid JSON.

                Required JSON structure:

                {
                  "category": "CATEGORY",
                  "explanation": "short explanation",
                  "evidence": [
                    {
                      "signal": "signal name",
                      "observation": "observed value",
                      "source": "observation"
                    }
                  ]
                }

                Observation:
                """);

        observation.forEach((key, value) ->
                prompt.append("\n")
                      .append(key)
                      .append(": ")
                      .append(value)
        );

        return prompt.toString();
    }
}