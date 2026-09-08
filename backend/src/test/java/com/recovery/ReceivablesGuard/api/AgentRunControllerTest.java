package com.recovery.ReceivablesGuard.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

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
import com.recovery.ReceivablesGuard.domain.AgentRunStatus;
import com.recovery.ReceivablesGuard.domain.Customer;
import com.recovery.ReceivablesGuard.domain.Invoice;
import com.recovery.ReceivablesGuard.domain.InvoiceStatus;
import com.recovery.ReceivablesGuard.repository.AgentRoundRepository;
import com.recovery.ReceivablesGuard.repository.AgentRunRepository;
import com.recovery.ReceivablesGuard.repository.CustomerRepository;
import com.recovery.ReceivablesGuard.repository.InvoiceRepository;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class AgentRunControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgentRunRepository agentRunRepository;

    @Autowired
    private AgentRoundRepository agentRoundRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Long agentRunId;
    private Long agentRoundId;

    @BeforeEach
    void setUp() {

        agentRoundRepository.deleteAll();
        agentRunRepository.deleteAll();
        invoiceRepository.deleteAll();
        customerRepository.deleteAll();

        Customer customer = new Customer(
                "TEST-CUSTOMER-RUN",
                "Agent Run Test Customer",
                "run-test@example.com",
                "8888888888"
        );

        customer = customerRepository.save(customer);

        Invoice invoice = new Invoice(
                "INV-RUN-001",
                customer,
                new BigDecimal("5000.00"),
                "INR",
                LocalDate.now().minusDays(20),
                LocalDate.now().minusDays(5)
        );

        invoice.setOutstandingAmount(new BigDecimal("5000.00"));
        invoice.setStatus(InvoiceStatus.OVERDUE);
        invoice.setDescription("Agent run REST API test invoice");

        invoice = invoiceRepository.save(invoice);

        AgentRun agentRun = new AgentRun(invoice, 3);
        agentRun.setStatus(AgentRunStatus.COMPLETED);
        agentRun.setStartedAt(Instant.now().minusSeconds(120));
        agentRun.setCompletedAt(Instant.now());

        agentRun = agentRunRepository.save(agentRun);

        AgentRound agentRound = new AgentRound(agentRun, 1);
        agentRound.setStatus(AgentRoundStatus.COMPLETED);
        agentRound.setDiagnosisCategory("PAYMENT_DELAY");
        agentRound.setPropensityScore(new BigDecimal("0.750000"));
        agentRound.setSelectedStrategy("EMAIL_REMINDER");
        agentRound.setAuthorized(true);
        agentRound.setStartedAt(Instant.now().minusSeconds(100));
        agentRound.setCompletedAt(Instant.now().minusSeconds(80));

        agentRound = agentRoundRepository.save(agentRound);

        agentRunId = agentRun.getId();
        agentRoundId = agentRound.getId();
    }

    @Test
    void getAgentRunReturnsOkAndRunData() throws Exception {

        mockMvc.perform(
                get("/api/agent-runs/{id}", agentRunId)
                        .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(agentRunId))
        .andExpect(jsonPath("$.invoiceReference").value("INV-RUN-001"))
        .andExpect(jsonPath("$.status").value("COMPLETED"))
        .andExpect(jsonPath("$.maxRounds").value(3))
        .andExpect(jsonPath("$.startedAt").exists())
        .andExpect(jsonPath("$.completedAt").exists())
        .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void getAgentRunRoundsReturnsOkAndRoundData() throws Exception {

        mockMvc.perform(
                get("/api/agent-runs/{id}/rounds", agentRunId)
                        .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].id").value(agentRoundId))
        .andExpect(jsonPath("$[0].agentRunId").value(agentRunId))
        .andExpect(jsonPath("$[0].roundNumber").value(1))
        .andExpect(jsonPath("$[0].status").value("COMPLETED"))
        .andExpect(jsonPath("$[0].diagnosisCategory").value("PAYMENT_DELAY"))
        .andExpect(jsonPath("$[0].propensityScore").value(0.75))
        .andExpect(jsonPath("$[0].selectedStrategy").value("EMAIL_REMINDER"))
        .andExpect(jsonPath("$[0].authorized").value(true));
    }

    @Test
    void getMissingAgentRunReturnsNotFound() throws Exception {

        mockMvc.perform(
                get("/api/agent-runs/{id}", 999999L)
                        .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound());
    }

    @Test
    void getRoundsForMissingAgentRunReturnsNotFound() throws Exception {

        mockMvc.perform(
                get("/api/agent-runs/{id}/rounds", 999999L)
                        .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound());
    }
}