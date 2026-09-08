package com.recovery.ReceivablesGuard.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.recovery.ReceivablesGuard.domain.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @EntityGraph(attributePaths = "customer")
    @Override
    List<Invoice> findAll();

    @Override
@EntityGraph(attributePaths = "customer")
Optional<Invoice> findById(Long id);

    Optional<Invoice> findByExternalRef(String externalRef);
}