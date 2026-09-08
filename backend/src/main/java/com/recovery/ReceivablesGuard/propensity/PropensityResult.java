package com.recovery.ReceivablesGuard.propensity;

import java.math.BigDecimal;

public record PropensityResult(
        BigDecimal probability,
        ScoreBreakdown breakdown
) {
}