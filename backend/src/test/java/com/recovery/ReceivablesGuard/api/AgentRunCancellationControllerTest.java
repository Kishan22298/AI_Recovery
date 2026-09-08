package com.recovery.ReceivablesGuard.api;

import java.math.BigDecimal;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.recovery.ReceivablesGuard.TestcontainersConfiguration;
import com.recovery.ReceivablesGuard.domain.AgentRun;
import com.recovery.ReceivablesGuard.domain.AgentRunStatus;
import com.recovery.ReceivablesGuard.domain.Customer;
import com.recovery.ReceivablesGuard.domain.Invoice;
import com.recovery.ReceivablesGuard.repository.AgentRunRepository;
import com.recovery.ReceivablesGuard.repository.CustomerRepository;
import com.recovery.ReceivablesGuard.repository.InvoiceRepository;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class AgentRunCancellationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgentRunRepository agentRunRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Long completedRunId;
    private Long cancelledRunId;

    @BeforeEach
    void setUp() {

        agentRunRepository.deleteAll();
        invoiceRepository.deleteAll();
        customerRepository.deleteAll();

        Customer customer = new Customer(
                "TEST-CANCEL-CUSTOMER",
                "Cancellation Test Customer",
                "cancel-test@example.com",
                "7777777777"
        );

        customer = customerRepository.save(customer);

        Invoice invoice = new Invoice(
                "INV-CANCEL-001",
                customer,
                new BigDecimal("5000.00"),
                "INR",
                LocalDate.now().minusDays(20),
                LocalDate.now().minusDays(5)
        );

        invoice = invoiceRepository.save(invoice);

        AgentRun completedRun = new AgentRun(invoice, 3);
        completedRun.setStatus(AgentRunStatus.COMPLETED);
        completedRun = agentRunRepository.save(completedRun);

        completedRunId = completedRun.getId();

        AgentRun cancelledRun = new AgentRun(invoice, 3);
        cancelledRun.setStatus(AgentRunStatus.CANCELLED);
        cancelledRun = agentRunRepository.save(cancelledRun);

        cancelledRunId = cancelledRun.getId();
    }

    @Test
    void completedRunCannotBeCancelled() throws Exception {

        mockMvc.perform(
                post("/api/agent-runs/{id}/cancel", completedRunId)
                        .accept(MediaType.TEXT_PLAIN)
        )
        .andExpect(status().isConflict())
        .andExpect(content().string(
                "Agent run cannot be cancelled"
        ));
    }

    @Test
    void alreadyCancelledRunCannotBeCancelled() throws Exception {

        mockMvc.perform(
                post("/api/agent-runs/{id}/cancel", cancelledRunId)
                        .accept(MediaType.TEXT_PLAIN)
        )
        .andExpect(status().isConflict())
        .andExpect(content().string(
                "Agent run cannot be cancelled"
        ));
    }
}