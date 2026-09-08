package com.recovery.ReceivablesGuard.simulation;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class SyntheticDataGeneratorTest {

    @Test
    void sameSeedProducesExactlySameResults() {

        SyntheticDataGenerator generator1 =
                new SyntheticDataGenerator();

        SyntheticDataGenerator generator2 =
                new SyntheticDataGenerator();

        List<SimulationResult> result1 =
                generator1.generate(42L, 100);

        List<SimulationResult> result2 =
                generator2.generate(42L, 100);

        assertEquals(result1, result2);
    }

    @Test
    void differentSeedCanProduceDifferentResults() {

        SyntheticDataGenerator generator1 =
                new SyntheticDataGenerator();

        SyntheticDataGenerator generator2 =
                new SyntheticDataGenerator();

        List<SimulationResult> result1 =
                generator1.generate(42L, 100);

        List<SimulationResult> result2 =
                generator2.generate(43L, 100);

        assertNotEquals(result1, result2);
    }

    @Test
    void generatedDataContainsKnownScenarios() {

        SyntheticDataGenerator generator =
                new SyntheticDataGenerator();

        List<SimulationResult> results =
                generator.generate(42L, 100);

        assertFalse(results.isEmpty());

        for (SimulationResult result : results) {

            assertNotNull(result.scenario());
            assertNotNull(result.customerReference());
            assertNotNull(result.invoiceReference());
        }
    }

    @Test
    void paymentNeverExceedsInvoiceAmount() {

        SyntheticDataGenerator generator =
                new SyntheticDataGenerator();

        List<SimulationResult> results =
                generator.generate(42L, 1000);

        for (SimulationResult result : results) {

            assertTrue(
                    result.paidAmount()
                            .compareTo(
                                    result.invoiceAmount()
                            ) <= 0
            );

            assertTrue(
                    result.outstandingAmount()
                            .compareTo(
                                    BigDecimal.ZERO
                            ) >= 0
            );
        }
    }

    @Test
    void allRequiredScenariosCanBeSimulated() {

        for (SimulationScenario scenario :
                SimulationScenario.values()) {

            CustomerResponseModel model =
                    new CustomerResponseModel();

            SimulationResult result =
                    model.simulate(
                            42L,
                            scenario,
                            "CUSTOMER-1",
                            "INVOICE-1",
                            new BigDecimal("10000.00"),
                            new SimulationClock()
                    );

            assertEquals(
                    scenario,
                    result.scenario()
            );
        }
    }
}