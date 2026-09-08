package com.recovery.ReceivablesGuard.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.recovery.ReceivablesGuard.domain.AgentRun;

public interface AgentRunRepository extends JpaRepository<AgentRun, Long> {

    @EntityGraph(attributePaths = "invoice")
List<AgentRun> findAllByInvoiceId(Long id);

    @Override
    @EntityGraph(attributePaths = "invoice")
    Optional<AgentRun> findById(Long id);
}