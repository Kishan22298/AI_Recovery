package com.recovery.ReceivablesGuard.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.recovery.ReceivablesGuard.domain.GuardrailConfig;

public interface GuardrailConfigRepository
        extends JpaRepository<GuardrailConfig, Long> {

    Optional<GuardrailConfig> findByConfigName(String configName);
}