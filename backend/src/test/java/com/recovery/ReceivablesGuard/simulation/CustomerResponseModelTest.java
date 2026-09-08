package com.recovery.ReceivablesGuard.simulation;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class CustomerResponseModelTest {

    private final CustomerResponseModel model =
            new CustomerResponseModel();

    private final SimulationClock clock =
            new SimulationClock();

    private final BigDecimal invoice =
            new BigDecimal("10000.00");

    @Test
    void paysScenarioPaysFullAmount() {

        SimulationResult result =
                model.simulate(
                        42L,
                        SimulationScenario.PAYS,
                        "C1",
                        "I1",
                        invoice,
                        clock
                );

        assertEquals(
                invoice,
                result.paidAmount()
        );

        assertEquals(
                BigDecimal.ZERO.setScale(2),
                result.outstandingAmount()
        );

        assertTrue(result.responded());
        assertFalse(result.failed());
    }

    @Test
    void noResponseScenarioPaysNothing() {

        SimulationResult result =
                model.simulate(
                        42L,
                        SimulationScenario.NO_RESPONSE,
                        "C1",
                        "I1",
                        invoice,
                        clock
                );

        assertEquals(
                BigDecimal.ZERO,
                result.paidAmount()
        );

        assertFalse(result.responded());
    }

    @Test
    void partialPaymentIsBetweenZeroAndFullAmount() {

        SimulationResult result =
                model.simulate(
                        42L,
                        SimulationScenario.PARTIAL_PAYS,
                        "C1",
                        "I1",
                        invoice,
                        clock
                );

        assertTrue(
                result.paidAmount()
                        .compareTo(BigDecimal.ZERO) > 0
        );

        assertTrue(
                result.paidAmount()
                        .compareTo(invoice) < 0
        );
    }

    @Test
    void promiseScenarioCreatesPromise() {

        SimulationResult result =
                model.simulate(
                        42L,
                        SimulationScenario.PROMISE,
                        "C1",
                        "I1",
                        invoice,
                        clock
                );

        assertTrue(result.responded());
        assertTrue(result.promisedPayment());
        assertFalse(result.promiseBroken());

        assertEquals(
                BigDecimal.ZERO,
                result.paidAmount()
        );
    }

    @Test
    void brokenPromiseScenarioIsMarkedCorrectly() {

        SimulationResult result =
                model.simulate(
                        42L,
                        SimulationScenario.BROKEN_PROMISE,
                        "C1",
                        "I1",
                        invoice,
                        clock
                );

        assertTrue(result.responded());
        assertTrue(result.promisedPayment());
        assertTrue(result.promiseBroken());
    }

    @Test
    void escalationScenarioIsMarkedCorrectly() {

        SimulationResult result =
                model.simulate(
                        42L,
                        SimulationScenario.ESCALATION,
                        "C1",
                        "I1",
                        invoice,
                        clock
                );

        assertTrue(result.escalated());
        assertFalse(result.failed());
    }

    @Test
    void failureScenarioIsMarkedCorrectly() {

        SimulationResult result =
                model.simulate(
                        42L,
                        SimulationScenario.FAILURE,
                        "C1",
                        "I1",
                        invoice,
                        clock
                );

        assertTrue(result.failed());
        assertFalse(result.responded());
    }
}