package com.recovery.ReceivablesGuard.metrics;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recovery.ReceivablesGuard.audit.AuditEvent;
import com.recovery.ReceivablesGuard.audit.AuditEventRepository;
import com.recovery.ReceivablesGuard.audit.AuditEventType;
import com.recovery.ReceivablesGuard.domain.InterventionOutcome;
import com.recovery.ReceivablesGuard.domain.Invoice;
import com.recovery.ReceivablesGuard.repository.InterventionOutcomeRepository;
import com.recovery.ReceivablesGuard.repository.InvoiceRepository;

@Service
public class RecoveryMetricsQueryService {

    private final InvoiceRepository invoiceRepository;

    private final InterventionOutcomeRepository
            interventionOutcomeRepository;

    private final AuditEventRepository
            auditEventRepository;

    private final RecoveryMetricsService
            recoveryMetricsService;

    public RecoveryMetricsQueryService(
            InvoiceRepository invoiceRepository,
            InterventionOutcomeRepository
                    interventionOutcomeRepository,
            AuditEventRepository auditEventRepository,
            RecoveryMetricsService recoveryMetricsService) {

        this.invoiceRepository = invoiceRepository;

        this.interventionOutcomeRepository =
                interventionOutcomeRepository;

        this.auditEventRepository =
                auditEventRepository;

        this.recoveryMetricsService =
                recoveryMetricsService;
    }

    @Transactional(readOnly = true)
    public RecoveryMetrics calculateMetrics() {

        List<Invoice> invoices =
                invoiceRepository.findAll();

        if (invoices.isEmpty()) {
            return recoveryMetricsService.calculateEmpty();
        }

        BigDecimal originalAmount =
                BigDecimal.ZERO;

        BigDecimal outstandingAmount =
                BigDecimal.ZERO;

        int interventionCount = 0;

        for (Invoice invoice : invoices) {

            if (invoice == null) {
                continue;
            }

            originalAmount =
                    originalAmount.add(
                            invoice.getTotalAmount()
                    );

            outstandingAmount =
                    outstandingAmount.add(
                            invoice.getOutstandingAmount()
                    );

            List<InterventionOutcome> outcomes =
                    interventionOutcomeRepository
                            .findAllByInvoiceId(
                                    invoice.getId()
                            );

            interventionCount += outcomes.size();
        }

        int blockedCount =
                countPolicyDecisions("BLOCKED");

        int escalations =
                countPolicyDecisions("HUMAN_ESCALATION");

        BigDecimal recoveredAmount =
                originalAmount
                        .subtract(outstandingAmount)
                        .max(BigDecimal.ZERO);

        BigDecimal recoveryRate =
                recoveryMetricsService
                        .calculateRecoveryRate(
                                originalAmount,
                                recoveredAmount
                        );

        return new RecoveryMetrics(
                outstandingAmount,
                recoveredAmount,
                recoveryRate,
                interventionCount,
                blockedCount,
                escalations,
                BigDecimal.ZERO,
                recoveredAmount,
                BigDecimal.ZERO
        );
    }

    private int countPolicyDecisions(
            String decision) {

        List<AuditEvent> policyEvents =
                auditEventRepository
                        .findAllByEventType(
                                AuditEventType.POLICY
                        );

        int count = 0;

        for (AuditEvent event : policyEvents) {

            if (event == null ||
                    event.getEventData() == null) {
                continue;
            }

            String eventData =
                    event.getEventData();

            String expected =
                    "\"decision\":\""
                            + decision
                            + "\"";

            if (eventData.contains(expected)) {
                count++;
            }
        }

        return count;
    }
}