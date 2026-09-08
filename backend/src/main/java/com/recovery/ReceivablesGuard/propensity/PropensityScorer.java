package com.recovery.ReceivablesGuard.propensity;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import com.recovery.ReceivablesGuard.observation.InvoiceContext;

@Service
public class PropensityScorer {

    private static final int SCALE = 6;

    private static final BigDecimal MIN_SCORE =
            BigDecimal.ZERO;

    private static final BigDecimal MAX_SCORE =
            BigDecimal.ONE;

    private static final BigDecimal BASE_SCORE =
            BigDecimal.valueOf(0.50);

    public PropensityResult score(InvoiceContext context) {

        if (context == null) {
            throw new IllegalArgumentException(
                    "InvoiceContext cannot be null"
            );
        }

        BigDecimal paymentHistoryAdjustment =
                paymentHistoryAdjustment(context);

        BigDecimal responsivenessAdjustment =
                responsivenessAdjustment(context);

        BigDecimal promiseAdjustment =
                promiseAdjustment(context);

        BigDecimal disputeAdjustment =
                disputeAdjustment(context);

        BigDecimal invoiceAgeAdjustment =
                invoiceAgeAdjustment(context);

        BigDecimal outstandingAmountAdjustment =
                outstandingAmountAdjustment(context);

        BigDecimal rawScore =
                BASE_SCORE
                        .add(paymentHistoryAdjustment)
                        .add(responsivenessAdjustment)
                        .add(promiseAdjustment)
                        .add(disputeAdjustment)
                        .add(invoiceAgeAdjustment)
                        .add(outstandingAmountAdjustment);

        BigDecimal finalScore =
                clamp(rawScore)
                        .setScale(SCALE, RoundingMode.HALF_UP);

        ScoreBreakdown breakdown =
                new ScoreBreakdown(
                        BASE_SCORE,
                        paymentHistoryAdjustment,
                        responsivenessAdjustment,
                        promiseAdjustment,
                        disputeAdjustment,
                        invoiceAgeAdjustment,
                        outstandingAmountAdjustment,
                        finalScore
                );

        return new PropensityResult(
                finalScore,
                breakdown
        );
    }

    private BigDecimal paymentHistoryAdjustment(
            InvoiceContext context) {

        Integer successfulPayments =
                context.successfulPayments();

        Integer failedPayments =
                context.failedPayments();

        if (successfulPayments == null &&
                failedPayments == null) {

            return BigDecimal.ZERO;
        }

        int successful =
                successfulPayments == null
                        ? 0
                        : Math.max(0, successfulPayments);

        int failed =
                failedPayments == null
                        ? 0
                        : Math.max(0, failedPayments);

        int total = successful + failed;

        if (total == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal successRate =
                BigDecimal.valueOf(successful)
                        .divide(
                                BigDecimal.valueOf(total),
                                SCALE,
                                RoundingMode.HALF_UP
                        );

        return successRate
                .subtract(BigDecimal.valueOf(0.5))
                .multiply(BigDecimal.valueOf(0.20));
    }

    private BigDecimal responsivenessAdjustment(
            InvoiceContext context) {

        Integer responsiveness =
                context.responsiveness();

        if (responsiveness == null) {
            return BigDecimal.ZERO;
        }

        int value =
                Math.max(0, Math.min(100, responsiveness));

        return BigDecimal.valueOf(value - 50)
                .divide(
                        BigDecimal.valueOf(100),
                        SCALE,
                        RoundingMode.HALF_UP
                )
                .multiply(BigDecimal.valueOf(0.20));
    }

    private BigDecimal promiseAdjustment(
            InvoiceContext context) {

        int adjustment = 0;

        if (Boolean.TRUE.equals(context.hasPromise())) {
            adjustment += 10;
        }

        if (Boolean.TRUE.equals(context.hasBrokenPromise())) {
            adjustment -= 20;
        }

        return BigDecimal.valueOf(adjustment)
                .divide(
                        BigDecimal.valueOf(100),
                        SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal disputeAdjustment(
            InvoiceContext context) {

        if (Boolean.TRUE.equals(context.hasDispute())) {
            return BigDecimal.valueOf(-0.20);
        }

        return BigDecimal.ZERO;
    }

    private BigDecimal invoiceAgeAdjustment(
            InvoiceContext context) {

        long age =
                context.invoiceAgeDays();

        if (age <= 0) {
            return BigDecimal.ZERO;
        }

        long days = Math.max(0, age);

        if (days <= 30) {
            return BigDecimal.valueOf(0.10);
        }

        if (days <= 60) {
            return BigDecimal.ZERO;
        }

        if (days <= 90) {
            return BigDecimal.valueOf(-0.10);
        }

        return BigDecimal.valueOf(-0.20);
    }

    private BigDecimal outstandingAmountAdjustment(
            InvoiceContext context) {

        BigDecimal outstanding =
                context.outstandingAmount();

        if (outstanding == null) {
            return BigDecimal.ZERO;
        }

        if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.valueOf(0.10);
        }

        if (outstanding.compareTo(
                BigDecimal.valueOf(10000)) <= 0) {

            return BigDecimal.ZERO;
        }

        if (outstanding.compareTo(
                BigDecimal.valueOf(100000)) <= 0) {

            return BigDecimal.valueOf(-0.05);
        }

        return BigDecimal.valueOf(-0.10);
    }

    private BigDecimal clamp(BigDecimal value) {

        if (value.compareTo(MIN_SCORE) < 0) {
            return MIN_SCORE;
        }

        if (value.compareTo(MAX_SCORE) > 0) {
            return MAX_SCORE;
        }

        return value;
    }
}