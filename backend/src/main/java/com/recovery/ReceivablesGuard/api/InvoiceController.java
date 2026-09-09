package com.recovery.ReceivablesGuard.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recovery.ReceivablesGuard.api.dto.AgentRunResponse;
import com.recovery.ReceivablesGuard.api.dto.InterventionOutcomeResponse;
import com.recovery.ReceivablesGuard.api.dto.InvoiceResponse;
import com.recovery.ReceivablesGuard.api.dto.PaymentResponse;
import com.recovery.ReceivablesGuard.api.dto.PromiseToPayResponse;
import com.recovery.ReceivablesGuard.repository.AgentRunRepository;
import com.recovery.ReceivablesGuard.repository.InterventionOutcomeRepository;
import com.recovery.ReceivablesGuard.repository.InvoiceRepository;
import com.recovery.ReceivablesGuard.repository.PaymentRepository;
import com.recovery.ReceivablesGuard.repository.PromiseToPayRepository;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;
    private final AgentRunRepository agentRunRepository;
    private final PaymentRepository paymentRepository;
    private final InterventionOutcomeRepository interventionOutcomeRepository;
    private final PromiseToPayRepository promiseToPayRepository;

    public InvoiceController(
            InvoiceRepository invoiceRepository,
            AgentRunRepository agentRunRepository,
            PaymentRepository paymentRepository,
            InterventionOutcomeRepository interventionOutcomeRepository,
            PromiseToPayRepository promiseToPayRepository) {

        this.invoiceRepository = invoiceRepository;
        this.agentRunRepository = agentRunRepository;
        this.paymentRepository = paymentRepository;
        this.interventionOutcomeRepository =
                interventionOutcomeRepository;
        this.promiseToPayRepository = promiseToPayRepository;
    }

    @GetMapping
    public List<InvoiceResponse> getInvoices() {

        return invoiceRepository.findAll()
                .stream()
                .map(InvoiceResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getInvoice(
            @PathVariable Long id) {

        return invoiceRepository.findById(id)
                .map(invoice ->
                        ResponseEntity.ok(
                                InvoiceResponse.from(invoice)
                        )
                )
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    @GetMapping("/{id}/agent-runs")
    public ResponseEntity<List<AgentRunResponse>> getAgentRuns(
            @PathVariable Long id) {

        if (!invoiceRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        List<AgentRunResponse> runs =
                agentRunRepository.findAllByInvoiceId(id)
                        .stream()
                        .map(AgentRunResponse::from)
                        .toList();

        return ResponseEntity.ok(runs);
    }

    @GetMapping("/{id}/payments")
    public ResponseEntity<List<PaymentResponse>> getPayments(
            @PathVariable Long id) {

        if (!invoiceRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        List<PaymentResponse> payments =
                paymentRepository.findAllByInvoiceId(id)
                        .stream()
                        .map(PaymentResponse::from)
                        .toList();

        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}/interventions")
    public ResponseEntity<List<InterventionOutcomeResponse>>
            getInterventions(@PathVariable Long id) {

        if (!invoiceRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        List<InterventionOutcomeResponse> outcomes =
                interventionOutcomeRepository
                        .findAllByInvoiceId(id)
                        .stream()
                        .map(InterventionOutcomeResponse::from)
                        .toList();

        return ResponseEntity.ok(outcomes);
    }

    @GetMapping("/{id}/promises")
    public ResponseEntity<List<PromiseToPayResponse>> getPromises(
            @PathVariable Long id) {

        if (!invoiceRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        List<PromiseToPayResponse> promises =
                promiseToPayRepository
                        .findAllByInvoiceId(id)
                        .stream()
                        .map(PromiseToPayResponse::from)
                        .toList();

        return ResponseEntity.ok(promises);
    }
}