package com.recovery.ReceivablesGuard.propensity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.recovery.ReceivablesGuard.observation.InvoiceContext;

class PropensityDeterminismTest {

    @Test
    void repeatedExecutionProducesIdenticalResult() {

        PropensityScorer scorer =
                new PropensityScorer();

        InvoiceContext context =
                InvoiceContext.forTesting(
                        BigDecimal.valueOf(75000),
                        75,
                        12,
                        3,
                        80,
                        2,
                        0,
                        true,
                        false,
                        false
                );

        PropensityResult result1 =
                scorer.score(context);

        PropensityResult result2 =
                scorer.score(context);

        assertEquals(
                result1.probability(),
                result2.probability()
        );

        assertEquals(
                result1.breakdown(),
                result2.breakdown()
        );
    }
}