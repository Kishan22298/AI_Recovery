-- ReceivablesGuard Phase 2: domain schema.
-- Flyway owns the real schema; Hibernate remains validation-only.

CREATE TABLE customers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    external_ref VARCHAR(100) NOT NULL,
    name VARCHAR(200) NOT NULL,
    email VARCHAR(320),
    phone VARCHAR(30),
    status VARCHAR(30) NOT NULL,
    do_not_contact BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT uk_customers_external_ref
        UNIQUE (external_ref),

    INDEX idx_customers_email (email),
    INDEX idx_customers_status (status)
) ENGINE=InnoDB;


CREATE TABLE invoices (
    id BIGINT NOT NULL AUTO_INCREMENT,
    external_ref VARCHAR(100) NOT NULL,
    customer_id BIGINT NOT NULL,

    total_amount DECIMAL(19,4) NOT NULL,
    outstanding_amount DECIMAL(19,4) NOT NULL,

    currency CHAR(3) NOT NULL,

    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,

    status VARCHAR(30) NOT NULL,

    description VARCHAR(2000),

    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT uk_invoices_external_ref
        UNIQUE (external_ref),

    CONSTRAINT ck_invoices_amounts
        CHECK (
            total_amount >= 0
            AND outstanding_amount >= 0
            AND outstanding_amount <= total_amount
        ),

    CONSTRAINT fk_invoices_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id),

    INDEX idx_invoices_customer_status
        (customer_id, status),

    INDEX idx_invoices_due_date
        (due_date),

    INDEX idx_invoices_status
        (status)
) ENGINE=InnoDB;


CREATE TABLE payments (
    id BIGINT NOT NULL AUTO_INCREMENT,

    invoice_id BIGINT NOT NULL,

    amount DECIMAL(19,4) NOT NULL,

    currency CHAR(3) NOT NULL,

    received_at DATETIME(6) NOT NULL,

    status VARCHAR(30) NOT NULL,

    reference VARCHAR(150),

    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT ck_payments_amount
        CHECK (amount > 0),

    CONSTRAINT fk_payments_invoice
        FOREIGN KEY (invoice_id)
        REFERENCES invoices(id),

    INDEX idx_payments_invoice
        (invoice_id),

    INDEX idx_payments_received_at
        (received_at)
) ENGINE=InnoDB;


CREATE TABLE communications (
    id BIGINT NOT NULL AUTO_INCREMENT,

    customer_id BIGINT NOT NULL,

    invoice_id BIGINT,

    channel VARCHAR(20) NOT NULL,

    direction VARCHAR(20) NOT NULL,

    status VARCHAR(30) NOT NULL,

    subject VARCHAR(100),

    sent_at DATETIME(6) NOT NULL,

    responded_at DATETIME(6),

    template_key VARCHAR(100),

    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT fk_communications_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id),

    CONSTRAINT fk_communications_invoice
        FOREIGN KEY (invoice_id)
        REFERENCES invoices(id),

    INDEX idx_communications_customer_time
        (customer_id, sent_at),

    INDEX idx_communications_invoice_time
        (invoice_id, sent_at),

    INDEX idx_communications_channel
        (channel)
) ENGINE=InnoDB;


CREATE TABLE promises_to_pay (
    id BIGINT NOT NULL AUTO_INCREMENT,

    invoice_id BIGINT NOT NULL,

    customer_id BIGINT NOT NULL,

    promised_amount DECIMAL(19,4) NOT NULL,

    promised_date DATE NOT NULL,

    status VARCHAR(30) NOT NULL,

    broken_at TIMESTAMP(6),

    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT ck_ptp_amount
        CHECK (promised_amount > 0),

    CONSTRAINT fk_ptp_invoice
        FOREIGN KEY (invoice_id)
        REFERENCES invoices(id),

    CONSTRAINT fk_ptp_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id),

    INDEX idx_ptp_invoice_status
        (invoice_id, status),

    INDEX idx_ptp_promised_date
        (promised_date)
) ENGINE=InnoDB;


CREATE TABLE agent_runs (
    id BIGINT NOT NULL AUTO_INCREMENT,

    invoice_id BIGINT NOT NULL,

    status VARCHAR(30) NOT NULL,

    max_rounds INT NOT NULL DEFAULT 3,

    started_at TIMESTAMP(6),

    completed_at TIMESTAMP(6),

    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT ck_agent_runs_max_rounds
        CHECK (max_rounds > 0),

    CONSTRAINT fk_agent_runs_invoice
        FOREIGN KEY (invoice_id)
        REFERENCES invoices(id),

    INDEX idx_agent_runs_invoice
        (invoice_id),

    INDEX idx_agent_runs_status
        (status),

    INDEX idx_agent_runs_started_at
        (started_at)
) ENGINE=InnoDB;


CREATE TABLE agent_rounds (
    id BIGINT NOT NULL AUTO_INCREMENT,

    agent_run_id BIGINT NOT NULL,

    round_number INT NOT NULL,

    status VARCHAR(30) NOT NULL,

    diagnosis_category VARCHAR(50),

    propensity_score DECIMAL(7,6),

    selected_strategy VARCHAR(50),

    authorized BOOLEAN NOT NULL DEFAULT FALSE,

    started_at TIMESTAMP(6),

    completed_at TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT uk_agent_rounds_run_number
        UNIQUE (agent_run_id, round_number),

    CONSTRAINT ck_agent_rounds_number
        CHECK (round_number > 0),

    CONSTRAINT ck_agent_rounds_propensity
        CHECK (
            propensity_score IS NULL
            OR (
                propensity_score >= 0
                AND propensity_score <= 1
            )
        ),

    CONSTRAINT fk_agent_rounds_run
        FOREIGN KEY (agent_run_id)
        REFERENCES agent_runs(id),

    INDEX idx_agent_rounds_run
        (agent_run_id),

    INDEX idx_agent_rounds_status
        (status)
) ENGINE=InnoDB;


CREATE TABLE intervention_outcomes (
    id BIGINT NOT NULL AUTO_INCREMENT,

    agent_round_id BIGINT NOT NULL,

    invoice_id BIGINT NOT NULL,

    outcome_type VARCHAR(40) NOT NULL,

    recovered_amount DECIMAL(19,4) NOT NULL DEFAULT 0,

    notes VARCHAR(2000),

    occurred_at TIMESTAMP(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT ck_outcomes_recovered_amount
        CHECK (recovered_amount >= 0),

    CONSTRAINT fk_outcomes_round
        FOREIGN KEY (agent_round_id)
        REFERENCES agent_rounds(id),

    CONSTRAINT fk_outcomes_invoice
        FOREIGN KEY (invoice_id)
        REFERENCES invoices(id),

    INDEX idx_outcomes_round
        (agent_round_id),

    INDEX idx_outcomes_invoice
        (invoice_id),

    INDEX idx_outcomes_type
        (outcome_type)
) ENGINE=InnoDB;


CREATE TABLE guardrail_configs (
    id BIGINT NOT NULL AUTO_INCREMENT,

    config_name VARCHAR(100) NOT NULL,

    contact_cap_per_week INT NOT NULL DEFAULT 3,

    cooldown_hours INT NOT NULL DEFAULT 24,

    discount_ceiling_percent DECIMAL(5,2) NOT NULL DEFAULT 10.00,

    monetary_escalation_threshold DECIMAL(19,4),

    business_hours_start TIME NOT NULL DEFAULT '09:00:00',

    business_hours_end TIME NOT NULL DEFAULT '18:00:00',

    broken_promise_lockout_hours INT NOT NULL DEFAULT 72,

    circuit_breaker_enabled BOOLEAN NOT NULL DEFAULT TRUE,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT uk_guardrail_configs_name
        UNIQUE (config_name),

    CONSTRAINT ck_guardrail_contact_cap
        CHECK (contact_cap_per_week >= 0),

    CONSTRAINT ck_guardrail_cooldown
        CHECK (cooldown_hours >= 0),

    CONSTRAINT ck_guardrail_discount
        CHECK (
            discount_ceiling_percent >= 0
            AND discount_ceiling_percent <= 100
        ),

    CONSTRAINT ck_guardrail_lockout
        CHECK (broken_promise_lockout_hours >= 0)
) ENGINE=InnoDB;

