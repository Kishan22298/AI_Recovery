package com.recovery.ReceivablesGuard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.recovery.ReceivablesGuard.domain.AgentRound;

public interface AgentRoundRepository
        extends JpaRepository<AgentRound, Long> {

    List<AgentRound> findAllByAgentRunId(Long id);
}