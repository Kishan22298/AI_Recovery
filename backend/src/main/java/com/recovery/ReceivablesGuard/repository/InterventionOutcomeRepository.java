package com.recovery.ReceivablesGuard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.recovery.ReceivablesGuard.domain.InterventionOutcome;

public interface InterventionOutcomeRepository
        extends JpaRepository<InterventionOutcome, Long> {

    List<InterventionOutcome> findAllByInvoiceId(Long id);
}