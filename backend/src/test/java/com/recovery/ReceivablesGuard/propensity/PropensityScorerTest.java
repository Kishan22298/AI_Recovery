package com.recovery.ReceivablesGuard.propensity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.recovery.ReceivablesGuard.observation.InvoiceContext;

class PropensityScorerTest {

    private final PropensityScorer scorer =
            new PropensityScorer();

    @Test
    void probabilityMustNeverBeBelowZero() {

        InvoiceContext context =
                InvoiceContext.forTesting(
                        BigDecimal.valueOf(500000),
                        365,
                        0,
                        100,
                        0,
                        0,
                        0,
                        true,
                        true,
                        true
                );

        PropensityResult result =
                scorer.score(context);

        assertTrue(
                result.probability()
                        .compareTo(BigDecimal.ZERO) >= 0
        );
    }

    @Test
    void probabilityMustNeverExceedOne() {

        InvoiceContext context =
                InvoiceContext.forTesting(
                        BigDecimal.ZERO,
                        1,
                        100,
                        0,
                        100,
                        0,
                        0,
                        true,
                        false,
                        false
                );

        PropensityResult result =
                scorer.score(context);

        assertTrue(
                result.probability()
                        .compareTo(BigDecimal.ONE) <= 0
        );
    }

    @Test
    void normalCaseProducesProbabilityBetweenZeroAndOne() {

        InvoiceContext context =
                InvoiceContext.forTesting(
                        BigDecimal.valueOf(50000),
                        45,
                        8,
                        2,
                        70,
                        10,
                        0,
                        true,
                        false,
                        false
                );

        PropensityResult result =
                scorer.score(context);

        assertTrue(
                result.probability()
                        .compareTo(BigDecimal.ZERO) >= 0
        );

        assertTrue(
                result.probability()
                        .compareTo(BigDecimal.ONE) <= 0
        );
    }

    @Test
    void sameContextProducesSameScore() {

        InvoiceContext context =
                InvoiceContext.forTesting(
                        BigDecimal.valueOf(25000),
                        60,
                        5,
                        1,
                        60,
                        5,
                        0,
                        true,
                        false,
                        false
                );

        PropensityResult first =
                scorer.score(context);

        PropensityResult second =
                scorer.score(context);

        assertEquals(
                first.probability(),
                second.probability()
        );

        assertEquals(
                first.breakdown(),
                second.breakdown()
        );
    }

    @Test
    void zeroPaymentHistoryIsHandled() {

        InvoiceContext context =
                InvoiceContext.forTesting(
                        BigDecimal.valueOf(10000),
                        30,
                        0,
                        0,
                        50,
                        0,
                        0,
                        false,
                        false,
                        false
                );

        PropensityResult result =
                scorer.score(context);

        assertNotNull(result);

        assertNotNull(result.probability());
    }

    @Test
    void disputeReducesScore() {

        InvoiceContext normal =
                InvoiceContext.forTesting(
                        BigDecimal.valueOf(10000),
                        30,
                        5,
                        1,
                        70,
                        0,
                        0,
                        false,
                        false,
                        false
                );

        InvoiceContext dispute =
                InvoiceContext.forTesting(
                        BigDecimal.valueOf(10000),
                        30,
                        5,
                        1,
                        70,
                        0,
                        0,
                        false,
                        false,
                        true
                );

        BigDecimal normalScore =
                scorer.score(normal).probability();

        BigDecimal disputeScore =
                scorer.score(dispute).probability();

        assertTrue(
                disputeScore.compareTo(normalScore) < 0
        );
    }

    @Test
    void brokenPromiseReducesScore() {

        InvoiceContext promise =
                InvoiceContext.forTesting(
                        BigDecimal.valueOf(10000),
                        30,
                        5,
                        1,
                        70,
                        1,      // promiseCount
                        0,      // brokenPromiseCount
                        true,
                        true,   // hasPromiseHistory
                        false
                );

        InvoiceContext brokenPromise =
                InvoiceContext.forTesting(
                        BigDecimal.valueOf(10000),
                        30,
                        5,
                        1,
                        70,
                        1,      // promiseCount
                        1,      // brokenPromiseCount
                        true,
                        true,   // hasPromiseHistory
                        false
                );

        BigDecimal promiseScore =
                scorer.score(promise).probability();

        BigDecimal brokenPromiseScore =
                scorer.score(brokenPromise).probability();

        assertTrue(
                brokenPromiseScore.compareTo(
                        promiseScore
                ) < 0
        );
    }
}