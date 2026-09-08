package com.recovery.ReceivablesGuard.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

class AuditServiceTest {

    @Test
    void lifecycleContainsAllRequiredEventTypes() {

        assertThat(AuditEventType.values())
                .containsExactly(
                        AuditEventType.OBSERVED_CONTEXT,
                        AuditEventType.DIAGNOSIS,
                        AuditEventType.EVIDENCE,
                        AuditEventType.CANDIDATE_STRATEGIES,
                        AuditEventType.PROPENSITY,
                        AuditEventType.EXPECTED_VALUE,
                        AuditEventType.RANKING,
                        AuditEventType.POLICY,
                        AuditEventType.BLOCKED_ACTIONS,
                        AuditEventType.FINAL_DECISION,
                        AuditEventType.EXECUTION,
                        AuditEventType.OUTCOME,
                        AuditEventType.STATE_UPDATE
                );
    }

    @Test
    void eventRejectsNullRunId() {

        AuditEventRepository repository = null;

        AuditService service = new AuditService(repository);

        assertThatThrownBy(() ->
                service.append(
                        null,
                        1L,
                        AuditEventType.DIAGNOSIS,
                        "SYSTEM",
                        "{}"
                )
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("agentRunId");
    }

    @Test
    void eventRejectsMissingActor() {

        AuditEventRepository repository = null;

        AuditService service = new AuditService(repository);

        assertThatThrownBy(() ->
                service.append(
                        1L,
                        1L,
                        AuditEventType.DIAGNOSIS,
                        "",
                        "{}"
                )
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("actor");
    }

    @Test
    void eventRejectsSecretFields() {

        AuditEventRepository repository = null;

        AuditService service = new AuditService(repository);

        assertThatThrownBy(() ->
                service.append(
                        1L,
                        1L,
                        AuditEventType.DIAGNOSIS,
                        "SYSTEM",
                        "{\"api_key\":\"DO_NOT_STORE\"}"
                )
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("forbidden secret");
    }

    @Test
    void missingLifecycleEventsCanBeDetected() {

        AuditService service = new AuditService(null);

        AuditEvent observed = new AuditEvent(
                1L,
                1L,
                1L,
                AuditEventType.OBSERVED_CONTEXT,
                "SYSTEM",
                "{}"
        );

        AuditEvent diagnosis = new AuditEvent(
                1L,
                1L,
                2L,
                AuditEventType.DIAGNOSIS,
                "SYSTEM",
                "{}"
        );

        List<AuditEventType> missing =
                service.missingLifecycleEvents(
                        List.of(observed, diagnosis)
                );

        assertThat(missing)
                .contains(
                        AuditEventType.EVIDENCE,
                        AuditEventType.PROPENSITY,
                        AuditEventType.EXPECTED_VALUE,
                        AuditEventType.RANKING,
                        AuditEventType.POLICY,
                        AuditEventType.BLOCKED_ACTIONS,
                        AuditEventType.FINAL_DECISION,
                        AuditEventType.EXECUTION,
                        AuditEventType.OUTCOME,
                        AuditEventType.STATE_UPDATE
                );
    }
}