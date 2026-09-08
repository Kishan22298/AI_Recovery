
package com.recovery.ReceivablesGuard;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.recovery.ReceivablesGuard.domain.AgentRound;
import com.recovery.ReceivablesGuard.domain.AgentRun;
import com.recovery.ReceivablesGuard.domain.Customer;
import com.recovery.ReceivablesGuard.domain.GuardrailConfig;
import com.recovery.ReceivablesGuard.domain.Invoice;
import com.recovery.ReceivablesGuard.domain.Payment;
import com.recovery.ReceivablesGuard.domain.PaymentStatus;
import com.recovery.ReceivablesGuard.domain.PromiseToPay;
import com.recovery.ReceivablesGuard.repository.AgentRoundRepository;
import com.recovery.ReceivablesGuard.repository.AgentRunRepository;
import com.recovery.ReceivablesGuard.repository.CustomerRepository;
import com.recovery.ReceivablesGuard.repository.GuardrailConfigRepository;
import com.recovery.ReceivablesGuard.repository.InvoiceRepository;
import com.recovery.ReceivablesGuard.repository.PaymentRepository;
import com.recovery.ReceivablesGuard.repository.PromiseToPayRepository;
import org.springframework.test.annotation.DirtiesContext;
import jakarta.persistence.EntityManager;


@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class Phase2PersistenceIntegrationTests {

        @Autowired
        EntityManager entityManager;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    InvoiceRepository invoiceRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    PromiseToPayRepository promiseToPayRepository;

    @Autowired
    AgentRunRepository agentRunRepository;

    @Autowired
    AgentRoundRepository agentRoundRepository;

    @Autowired
    GuardrailConfigRepository guardrailConfigRepository;

    @Test
    void persistsAggregateAndRelationships() {

        Customer customer = customerRepository.save(
                new Customer(
                        "CUST-P2-001",
                        "Phase Two Customer",
                        "p2@example.com",
                        "+919999999999"
                )
        );

        Invoice invoice = invoiceRepository.save(
                new Invoice(
                        "INV-P2-001",
                        customer,
                        new BigDecimal("10000.00"),
                        "INR",
                        LocalDate.now().minusDays(30),
                        LocalDate.now().minusDays(10)
                )
        );

        Payment payment = paymentRepository.save(
                new Payment(
                        invoice,
                        new BigDecimal("2500.00"),
                        "INR",
                        java.time.LocalDateTime.now(),
                        PaymentStatus.SETTLED,
                        "PAY-P2-001"
                )
        );

        PromiseToPay ptp = promiseToPayRepository.save(
                new PromiseToPay(
                        invoice,
                        customer,
                        new BigDecimal("5000.00"),
                        LocalDate.now().plusDays(5)
                )
        );

        AgentRun run = agentRunRepository.save(
                new AgentRun(invoice, 3)
        );

        AgentRound round = agentRoundRepository.save(
                new AgentRound(run, 1)
        );

        assertThat(
                customerRepository
                        .findByExternalRef("CUST-P2-001")
        ).isPresent();

        assertThat(
                invoiceRepository
                        .findByExternalRef("INV-P2-001")
        ).isPresent();

        assertThat(
                paymentRepository
                        .findAllByInvoiceId(invoice.getId())
        ).hasSize(1);

        assertThat(
                promiseToPayRepository
                        .findAllByInvoiceId(invoice.getId())
        ).hasSize(1);

        assertThat(
                agentRoundRepository
                        .findAllByAgentRunId(run.getId())
        ).hasSize(1);

        assertThat(round.getAgentRun().getId())
                .isEqualTo(run.getId());

        assertThat(ptp.getInvoice().getId())
                .isEqualTo(invoice.getId());
    }

    @Test
    void uniqueAndForeignKeyConstraintsAreEnforced() {

        Customer first = customerRepository.save(
                new Customer(
                        "CUST-P2-UNIQUE",
                        "Unique Customer",
                        null,
                        null
                )
        );

        assertThatThrownBy(() ->
                customerRepository.saveAndFlush(
                        new Customer(
                                "CUST-P2-UNIQUE",
                                "Duplicate Customer",
                                null,
                                null
                        )
                )
        )
        .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(first.getId()).isNotNull();
    }
@Test
void guardrailDefaultIsSeededByFlyway() {

    System.out.println("========== GUARDRAIL DEBUG ==========");

    System.out.println(
            "Total guardrails = " + guardrailConfigRepository.count()
    );

    Long nativeCount = ((Number)entityManager
            .createNativeQuery("SELECT COUNT(*) FROM guardrail_configs")
            .getSingleResult())
            .longValue();

    System.out.println(
            "NATIVE guardrail_configs COUNT = " + nativeCount
    );

    guardrailConfigRepository.findAll().forEach(config ->
            System.out.println(
                    "Guardrail: id=" + config.getId()
                            + ", name=" + config.getConfigName()
                            + ", cap=" + config.getContactCapPerWeek()
            )
    );

    System.out.println(
            "Default lookup = "
                    + guardrailConfigRepository.findByConfigName("default")
    );

    System.out.println("=====================================");

    assertThat(
            guardrailConfigRepository.findByConfigName("default")
    )
    .isPresent()
    .get()
    .extracting(GuardrailConfig::getContactCapPerWeek)
    .isEqualTo(3);
}
}
