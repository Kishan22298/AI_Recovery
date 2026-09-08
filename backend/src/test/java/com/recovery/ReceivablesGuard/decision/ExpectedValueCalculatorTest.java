package com.recovery.ReceivablesGuard.decision;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class ExpectedValueCalculatorTest {

    private final ExpectedValueCalculator calculator =
            new ExpectedValueCalculator();

    @Test
    void calculatesPositiveExpectedValue() {

        BigDecimal result =
                calculator.calculate(
                        new BigDecimal("0.80"),
                        new BigDecimal("1000.00"),
                        new BigDecimal("50.00")
                );

        assertEquals(
                new BigDecimal("750.00"),
                result
        );
    }

    @Test
    void calculatesZeroExpectedValue() {

        BigDecimal result =
                calculator.calculate(
                        new BigDecimal("0.05"),
                        new BigDecimal("100.00"),
                        new BigDecimal("5.00")
                );

        assertEquals(
                new BigDecimal("0.00"),
                result
        );
    }

    @Test
    void calculatesNegativeExpectedValue() {

        BigDecimal result =
                calculator.calculate(
                        new BigDecimal("0.10"),
                        new BigDecimal("100.00"),
                        new BigDecimal("20.00")
                );

        assertEquals(
                new BigDecimal("-10.00"),
                result
        );
    }

    @Test
    void probabilityZeroProducesNegativeCost() {

        BigDecimal result =
                calculator.calculate(
                        BigDecimal.ZERO,
                        new BigDecimal("1000.00"),
                        new BigDecimal("25.00")
                );

        assertEquals(
                new BigDecimal("-25.00"),
                result
        );
    }

    @Test
    void probabilityOneProducesOutstandingMinusCost() {

        BigDecimal result =
                calculator.calculate(
                        BigDecimal.ONE,
                        new BigDecimal("1000.00"),
                        new BigDecimal("25.00")
                );

        assertEquals(
                new BigDecimal("975.00"),
                result
        );
    }

    @Test
    void rejectsProbabilityBelowZero() {

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        new BigDecimal("-0.01"),
                        new BigDecimal("1000.00"),
                        new BigDecimal("10.00")
                )
        );
    }

    @Test
    void rejectsProbabilityAboveOne() {

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        new BigDecimal("1.01"),
                        new BigDecimal("1000.00"),
                        new BigDecimal("10.00")
                )
        );
    }

    @Test
    void repeatedExecutionIsDeterministic() {

        BigDecimal first =
                calculator.calculate(
                        new BigDecimal("0.73"),
                        new BigDecimal("1234.56"),
                        new BigDecimal("25.00")
                );

        BigDecimal second =
                calculator.calculate(
                        new BigDecimal("0.73"),
                        new BigDecimal("1234.56"),
                        new BigDecimal("25.00")
                );

        assertEquals(first, second);
    }
}