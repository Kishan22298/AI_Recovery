package com.recovery.ReceivablesGuard.policy;

public enum PolicyRule {

    CONTACT_CAP,
    COOLDOWN,
    DISCOUNT_LIMIT,
    AMOUNT_THRESHOLD,
    DO_NOT_CONTACT,
    BROKEN_PROMISE_LOCKOUT,
    BUSINESS_HOURS,
    CIRCUIT_BREAKER,
    SAFETY_RESTRICTION
}