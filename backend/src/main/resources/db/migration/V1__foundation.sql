-- ReceivablesGuard Phase 1 foundation migration.
-- Business tables will be added in later phases.

-- ReceivablesGuard Phase 1 foundation migration.

CREATE TABLE IF NOT EXISTS schema_version_marker (
    id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);