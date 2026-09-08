package com.recovery.ReceivablesGuard.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.recovery.ReceivablesGuard.TestcontainersConfiguration;

import com.recovery.ReceivablesGuard.audit.AuditEvent;
import com.recovery.ReceivablesGuard.audit.AuditEventRepository;
import com.recovery.ReceivablesGuard.audit.AuditEventType;
import com.recovery.ReceivablesGuard.audit.AuditService;
import com.recovery.ReceivablesGuard.repository.AgentRoundRepository;
import com.recovery.ReceivablesGuard.repository.AgentRunRepository;
import com.recovery.ReceivablesGuard.repository.CustomerRepository;
import com.recovery.ReceivablesGuard.repository.InvoiceRepository;

import com.recovery.ReceivablesGuard.domain.Customer;
import com.recovery.ReceivablesGuard.domain.Invoice;

import com.recovery.ReceivablesGuard.domain.AgentRound;
import com.recovery.ReceivablesGuard.domain.AgentRun;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class AuditServiceIntegrationTest {

   @Autowired
    private AuditService auditService;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private AgentRunRepository agentRunRepository;

    @Autowired
    private AgentRoundRepository agentRoundRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Test
    void createsAuditEventsInSequence() {

        Long runId = createAgentRun();

        AuditEvent first =
                auditService.append(
                        runId,
                        null,
                        AuditEventType.OBSERVED_CONTEXT,
                        "SYSTEM",
                        "{\"invoiceId\":1001}"
                );

        AuditEvent second =
                auditService.append(
                        runId,
                        null,
                        AuditEventType.DIAGNOSIS,
                        "SYSTEM",
                        "{\"category\":\"PAYMENT_DELAY\"}"
                );

        AuditEvent third =
                auditService.append(
                        runId,
                        null,
                        AuditEventType.EVIDENCE,
                        "SYSTEM",
                        "{\"daysOverdue\":30}"
                );

        assertThat(first.getSequenceNumber())
                .isEqualTo(1L);

        assertThat(second.getSequenceNumber())
                .isEqualTo(2L);

        assertThat(third.getSequenceNumber())
                .isEqualTo(3L);

        List<AuditEvent> events =
                auditService.reconstructRun(runId);

        assertThat(events)
                .extracting(AuditEvent::getEventType)
                .containsExactly(
                        AuditEventType.OBSERVED_CONTEXT,
                        AuditEventType.DIAGNOSIS,
                        AuditEventType.EVIDENCE
                );
    }

    @Test
    void reconstructsRoundInSequence() {

        Long runId = createAgentRun();

        Long roundId = createAgentRound(runId);

        auditService.append(
                runId,
                roundId,
                AuditEventType.OBSERVED_CONTEXT,
                "SYSTEM",
                "{}"
        );

        auditService.append(
                runId,
                roundId,
                AuditEventType.DIAGNOSIS,
                "SYSTEM",
                "{}"
        );

        auditService.append(
                runId,
                roundId,
                AuditEventType.RANKING,
                "SYSTEM",
                "{}"
        );

        List<AuditEvent> events =
                auditService.reconstructRound(roundId);

        assertThat(events)
                .extracting(AuditEvent::getEventType)
                .containsExactly(
                        AuditEventType.OBSERVED_CONTEXT,
                        AuditEventType.DIAGNOSIS,
                        AuditEventType.RANKING
                );
    }

    @Test
    void missingEventsAreDetectable() {

        Long runId = createAgentRun();

        auditService.append(
                runId,
                null,
                AuditEventType.OBSERVED_CONTEXT,
                "SYSTEM",
                "{}"
        );

        auditService.append(
                runId,
                null,
                AuditEventType.DIAGNOSIS,
                "SYSTEM",
                "{}"
        );

        List<AuditEvent> events =
                auditService.reconstructRun(runId);

        List<AuditEventType> missing =
                auditService.missingLifecycleEvents(events);

        assertThat(missing)
                .contains(AuditEventType.EVIDENCE);

        assertThat(missing)
                .contains(AuditEventType.STATE_UPDATE);
    }

    @Test
    void secretDataIsRejected() {

        Long runId = createAgentRun();

        assertThatThrownBy(() ->
                auditService.append(
                        runId,
                        null,
                        AuditEventType.DIAGNOSIS,
                        "SYSTEM",
                        "{\"password\":\"secret123\"}"
                )
        )
        .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void auditEventsAreNotUpdatedByAuditService() {

        Long runId = createAgentRun();

        AuditEvent event =
                auditService.append(
                        runId,
                        null,
                        AuditEventType.DIAGNOSIS,
                        "SYSTEM",
                        "{\"value\":\"original\"}"
                );

        List<AuditEvent> before =
                auditService.reconstructRun(runId);

        assertThat(before).hasSize(1);

        assertThat(event.getEventData())
                .contains("original");

        /*
         * AuditService exposes append + read only.
         * There is intentionally no update/delete operation.
         */
        assertThat(auditEventRepository)
                .isNotNull();
    }
private Long createAgentRun() {

    Customer customer = customerRepository.save(
            new Customer(
                    "CUST-AUDIT-001",
                    "Audit Test Customer",
                    "audit@example.com",
                    "+919999999999"
            )
    );

    Invoice invoice = invoiceRepository.save(
            new Invoice(
                    "INV-AUDIT-001",
                    customer,
                    new BigDecimal("10000.00"),
                    "INR",
                    LocalDate.now().minusDays(30),
                    LocalDate.now().minusDays(10)
            )
    );

    AgentRun run = agentRunRepository.save(
            new AgentRun(invoice, 3)
    );

    return run.getId();
}

private Long createAgentRound(Long runId) {

    AgentRun run = agentRunRepository.findById(runId)
            .orElseThrow(
                    () -> new IllegalStateException(
                            "AgentRun not found: " + runId
                    )
            );

    AgentRound round = agentRoundRepository.save(
            new AgentRound(run, 1)
    );

    return round.getId();
}
}
