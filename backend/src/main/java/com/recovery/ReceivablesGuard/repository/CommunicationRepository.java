package com.recovery.ReceivablesGuard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.recovery.ReceivablesGuard.domain.Communication;

public interface CommunicationRepository
        extends JpaRepository<Communication, Long> {

    List<Communication> findAllByCustomerId(Long id);
}