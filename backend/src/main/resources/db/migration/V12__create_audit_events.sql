-- ReceivablesGuard Phase 12: append-only audit events.
-- Audit events reconstruct the complete agent decision lifecycle.

CREATE TABLE audit_events (
    id BIGINT NOT NULL AUTO_INCREMENT,

    agent_run_id BIGINT NOT NULL,

    agent_round_id BIGINT,

    sequence_number BIGINT NOT NULL,

    event_type VARCHAR(50) NOT NULL,

    actor VARCHAR(50) NOT NULL,

    event_data JSON,

    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT uk_audit_events_run_sequence
        UNIQUE (agent_run_id, sequence_number),

    CONSTRAINT ck_audit_events_sequence
        CHECK (sequence_number > 0),

    CONSTRAINT fk_audit_events_run
        FOREIGN KEY (agent_run_id)
        REFERENCES agent_runs(id),

    CONSTRAINT fk_audit_events_round
        FOREIGN KEY (agent_round_id)
        REFERENCES agent_rounds(id),

    INDEX idx_audit_events_run_sequence
        (agent_run_id, sequence_number),

    INDEX idx_audit_events_round_sequence
        (agent_round_id, sequence_number),

    INDEX idx_audit_events_type
        (event_type),

    INDEX idx_audit_events_created_at
        (created_at)
) ENGINE=InnoDB;