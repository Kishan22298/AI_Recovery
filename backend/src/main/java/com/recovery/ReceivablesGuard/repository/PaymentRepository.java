package com.recovery.ReceivablesGuard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.recovery.ReceivablesGuard.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByInvoiceId(Long id);
}