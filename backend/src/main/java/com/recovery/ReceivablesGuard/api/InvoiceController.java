package com.recovery.ReceivablesGuard.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recovery.ReceivablesGuard.api.dto.AgentRunResponse;
import com.recovery.ReceivablesGuard.api.dto.InvoiceResponse;
import com.recovery.ReceivablesGuard.repository.AgentRunRepository;
import com.recovery.ReceivablesGuard.repository.InvoiceRepository;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;
    private final AgentRunRepository agentRunRepository;

    public InvoiceController(
            InvoiceRepository invoiceRepository,
            AgentRunRepository agentRunRepository) {

        this.invoiceRepository = invoiceRepository;
        this.agentRunRepository = agentRunRepository;
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
}