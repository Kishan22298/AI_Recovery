package com.recovery.ReceivablesGuard.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.recovery.ReceivablesGuard.domain.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByExternalRef(String externalRef);
}