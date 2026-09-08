package com.recovery.ReceivablesGuard.policy;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;

import org.springframework.stereotype.Component;

@Component
public class PolicyConfiguration {

    private final int maxContactsPerDay;
    private final Duration cooldown;
    private final BigDecimal maxDiscountPercentage;
    private final BigDecimal maximumAllowedAmount;
    private final LocalTime businessHoursStart;
    private final LocalTime businessHoursEnd;
    private final boolean circuitBreakerEnabled;

    public PolicyConfiguration() {

        this.maxContactsPerDay = 3;

        this.cooldown = Duration.ofHours(24);

        this.maxDiscountPercentage =
                new BigDecimal("10.00");

        this.maximumAllowedAmount =
                new BigDecimal("1000000.00");

        this.businessHoursStart =
                LocalTime.of(9, 0);

        this.businessHoursEnd =
                LocalTime.of(18, 0);

        this.circuitBreakerEnabled = false;
    }

    public int maxContactsPerDay() {
        return maxContactsPerDay;
    }

    public Duration cooldown() {
        return cooldown;
    }

    public BigDecimal maxDiscountPercentage() {
        return maxDiscountPercentage;
    }

    public BigDecimal maximumAllowedAmount() {
        return maximumAllowedAmount;
    }

    public LocalTime businessHoursStart() {
        return businessHoursStart;
    }

    public LocalTime businessHoursEnd() {
        return businessHoursEnd;
    }

    public boolean circuitBreakerEnabled() {
        return circuitBreakerEnabled;
    }
}