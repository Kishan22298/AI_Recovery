INSERT INTO guardrail_configs (
    config_name,
    contact_cap_per_week,
    cooldown_hours,
    discount_ceiling_percent,
    broken_promise_lockout_hours,
    circuit_breaker_enabled,
    active
)
VALUES (
    'default',
    3,
    24,
    10.00,
    72,
    TRUE,
    TRUE
);