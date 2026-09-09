package com.recovery.ReceivablesGuard.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.recovery.ReceivablesGuard.TestcontainersConfiguration;
import com.recovery.ReceivablesGuard.domain.AgentRound;
import com.recovery.ReceivablesGuard.domain.AgentRoundStatus;
import com.recovery.ReceivablesGuard.domain.AgentRun;
import com.recovery.ReceivablesGuard.domain.Customer;
import com.recovery.ReceivablesGuard.domain.InterventionOutcome;
import com.recovery.ReceivablesGuard.domain.Invoice;
import com.recovery.ReceivablesGuard.domain.InvoiceStatus;
import com.recovery.ReceivablesGuard.domain.OutcomeType;
import com.recovery.ReceivablesGuard.domain.Payment;
import com.recovery.ReceivablesGuard.domain.PaymentStatus;
import com.recovery.ReceivablesGuard.domain.PromiseStatus;
import com.recovery.ReceivablesGuard.domain.PromiseToPay;
import com.recovery.ReceivablesGuard.repository.AgentRoundRepository;
import com.recovery.ReceivablesGuard.repository.AgentRunRepository;
import com.recovery.ReceivablesGuard.repository.CustomerRepository;
import com.recovery.ReceivablesGuard.repository.InterventionOutcomeRepository;
import com.recovery.ReceivablesGuard.repository.InvoiceRepository;
import com.recovery.ReceivablesGuard.repository.PaymentRepository;
import com.recovery.ReceivablesGuard.repository.PromiseToPayRepository;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AgentRunRepository agentRunRepository;
    
    @Autowired
private PaymentRepository paymentRepository;

@Autowired
private InterventionOutcomeRepository interventionOutcomeRepository;

@Autowired
private PromiseToPayRepository promiseToPayRepository;

@Autowired
private AgentRoundRepository agentRoundRepository;

    private Long invoiceId;

    @BeforeEach
    void setUp() {
        interventionOutcomeRepository.deleteAll();
paymentRepository.deleteAll();
promiseToPayRepository.deleteAll();
agentRoundRepository.deleteAll();
agentRunRepository.deleteAll();
invoiceRepository.deleteAll();
customerRepository.deleteAll();

        Customer customer = new Customer(
                "TEST-CUSTOMER",
                "Test Customer",
                "test@example.com",
                "9999999999"
        );

        customer = customerRepository.save(customer);

        Invoice invoice = new Invoice(
                "INV-REST-001",
                customer,
                new BigDecimal("10000.00"),
                "INR",
                LocalDate.now().minusDays(30),
                LocalDate.now().minusDays(10)
        );

        invoice.setOutstandingAmount(new BigDecimal("7500.00"));
        invoice.setStatus(InvoiceStatus.OVERDUE);
        invoice.setDescription("REST API test invoice");

        invoice = invoiceRepository.save(invoice);

        invoiceId = invoice.getId();
    }

    @Test
    void getAllInvoicesReturnsOkAndInvoiceData() throws Exception {

        mockMvc.perform(
                get("/api/invoices")
                        .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].id").value(invoiceId))
        .andExpect(jsonPath("$[0].externalRef").value("INV-REST-001"))
        .andExpect(jsonPath("$[0].customerReference").value("TEST-CUSTOMER"))
        .andExpect(jsonPath("$[0].totalAmount").value(10000.00))
        .andExpect(jsonPath("$[0].outstandingAmount").value(7500.00))
        .andExpect(jsonPath("$[0].currency").value("INR"))
        .andExpect(jsonPath("$[0].status").value("OVERDUE"));
    }

    @Test
    void getInvoiceByIdReturnsOkAndInvoiceData() throws Exception {

        mockMvc.perform(
                get("/api/invoices/{id}", invoiceId)
                        .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(invoiceId))
        .andExpect(jsonPath("$.externalRef").value("INV-REST-001"))
        .andExpect(jsonPath("$.customerReference").value("TEST-CUSTOMER"))
        .andExpect(jsonPath("$.totalAmount").value(10000.00))
        .andExpect(jsonPath("$.outstandingAmount").value(7500.00))
        .andExpect(jsonPath("$.currency").value("INR"))
        .andExpect(jsonPath("$.status").value("OVERDUE"));
    }

    @Test
    void getMissingInvoiceReturnsNotFound() throws Exception {

        mockMvc.perform(
                get("/api/invoices/{id}", 999999L)
                        .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound());
    }

    @Test
    void getAgentRunsForInvoiceReturnsEmptyListWhenNoRunsExist()
            throws Exception {

        mockMvc.perform(
                get("/api/invoices/{id}/agent-runs", invoiceId)
                        .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
void getAgentRunsForInvoiceReturnsRunData()
        throws Exception {

    Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow();

    AgentRun agentRun = new AgentRun(
            invoice,
            1
    );

    agentRun = agentRunRepository.save(agentRun);

    mockMvc.perform(
            get("/api/invoices/{id}/agent-runs", invoiceId)
                    .accept(MediaType.APPLICATION_JSON)
    )
    .andExpect(status().isOk())
    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
    .andExpect(jsonPath("$").isArray())
    .andExpect(jsonPath("$.length()").value(1))
    .andExpect(jsonPath("$[0].id").value(agentRun.getId()))
    .andExpect(jsonPath("$[0].invoiceId").value(invoiceId))
    .andExpect(jsonPath("$[0].invoiceReference").value("INV-REST-001"))
    .andExpect(jsonPath("$[0].maxRounds").value(1));
}
    @Test
    void getAgentRunsForMissingInvoiceReturnsNotFound()
            throws Exception {

        mockMvc.perform(
                get("/api/invoices/{id}/agent-runs", 999999L)
                        .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound());
    }

    @Test
void getPaymentsForInvoiceReturnsPaymentData()
        throws Exception {

    Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow();

    Payment payment = new Payment(
            invoice,
            new BigDecimal("2500.00"),
            "INR",
            LocalDateTime.now().minusDays(2),
            PaymentStatus.SETTLED,
            "PAY-REST-001"
    );

    payment = paymentRepository.save(payment);

    mockMvc.perform(
            get("/api/invoices/{id}/payments", invoiceId)
                    .accept(MediaType.APPLICATION_JSON)
    )
    .andExpect(status().isOk())
    .andExpect(content().contentTypeCompatibleWith(
            MediaType.APPLICATION_JSON))
    .andExpect(jsonPath("$").isArray())
    .andExpect(jsonPath("$.length()").value(1))
    .andExpect(jsonPath("$[0].id").value(payment.getId()))
    .andExpect(jsonPath("$[0].amount").value(2500.00))
    .andExpect(jsonPath("$[0].currency").value("INR"))
    .andExpect(jsonPath("$[0].status").value("SETTLED"))
    .andExpect(jsonPath("$[0].reference").value("PAY-REST-001"));
}

@Test
void getInterventionsForInvoiceReturnsOutcomeData()
        throws Exception {

    Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow();

    AgentRun agentRun = new AgentRun(
            invoice,
            1
    );

    agentRun = agentRunRepository.save(agentRun);

    AgentRound round = new AgentRound(
            agentRun,
            1
    );

    round.setStatus(AgentRoundStatus.COMPLETED);
    round.setSelectedStrategy("EMAIL_REMINDER");

    round = agentRoundRepository.save(round);

    InterventionOutcome outcome = new InterventionOutcome(
            round,
            invoice,
            OutcomeType.PARTIAL_PAYMENT,
            new BigDecimal("2500.00"),
            "Customer made a partial payment.",
            Instant.now()
    );

    outcome = interventionOutcomeRepository.save(outcome);

    mockMvc.perform(
            get("/api/invoices/{id}/interventions", invoiceId)
                    .accept(MediaType.APPLICATION_JSON)
    )
    .andExpect(status().isOk())
    .andExpect(content().contentTypeCompatibleWith(
            MediaType.APPLICATION_JSON))
    .andExpect(jsonPath("$").isArray())
    .andExpect(jsonPath("$.length()").value(1))
    .andExpect(jsonPath("$[0].id").value(outcome.getId()))
    .andExpect(jsonPath("$[0].agentRoundId").value(round.getId()))
    .andExpect(jsonPath("$[0].outcomeType")
            .value("PARTIAL_PAYMENT"))
    .andExpect(jsonPath("$[0].recoveredAmount").value(2500.00))
    .andExpect(jsonPath("$[0].notes")
            .value("Customer made a partial payment."));
}

@Test
void getPromisesForInvoiceReturnsPromiseData()
        throws Exception {

    Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow();

    Customer customer = customerRepository.findById(
            invoice.getCustomer().getId()
    ).orElseThrow();

    PromiseToPay promise = new PromiseToPay(
            invoice,
            customer,
            new BigDecimal("5000.00"),
            LocalDate.now().plusDays(7)
    );

    promise.setStatus(PromiseStatus.OPEN);

    promise = promiseToPayRepository.save(promise);

    mockMvc.perform(
            get("/api/invoices/{id}/promises", invoiceId)
                    .accept(MediaType.APPLICATION_JSON)
    )
    .andExpect(status().isOk())
    .andExpect(content().contentTypeCompatibleWith(
            MediaType.APPLICATION_JSON))
    .andExpect(jsonPath("$").isArray())
    .andExpect(jsonPath("$.length()").value(1))
    .andExpect(jsonPath("$[0].id").value(promise.getId()))
    .andExpect(jsonPath("$[0].promisedAmount").value(5000.00))
    .andExpect(jsonPath("$[0].status").value("OPEN"))
    .andExpect(jsonPath("$[0].promisedDate")
            .value(LocalDate.now().plusDays(7).toString()));
}
@Test
void getPaymentsForMissingInvoiceReturnsNotFound()
        throws Exception {

    mockMvc.perform(
            get("/api/invoices/{id}/payments", 999999L)
                    .accept(MediaType.APPLICATION_JSON)
    )
    .andExpect(status().isNotFound());
}
}