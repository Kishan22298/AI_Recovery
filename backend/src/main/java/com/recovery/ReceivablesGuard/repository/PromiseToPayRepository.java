package com.recovery.ReceivablesGuard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.recovery.ReceivablesGuard.domain.PromiseToPay;

public interface PromiseToPayRepository
        extends JpaRepository<PromiseToPay, Long> {

    List<PromiseToPay> findAllByInvoiceId(Long id);
}