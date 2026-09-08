package com.recovery.ReceivablesGuard.simulation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;

/**
 * Generates deterministic synthetic simulation data.
 *
 * No database writes.
 * No email.
 * No payment.
 * No external API calls.
 */
@Component
public class SyntheticDataGenerator {

    private static final BigDecimal MIN_INVOICE =
            new BigDecimal("1000.00");

    private static final BigDecimal MAX_INVOICE =
            new BigDecimal("100000.00");

    public List<SimulationResult> generate(long seed, long count) {

        if (count < 0) {
            throw new IllegalArgumentException(
                    "count must not be negative"
            );
        }

        Random random = new Random(seed);

        SimulationClock clock =
                new SimulationClock();

        CustomerResponseModel responseModel =
                new CustomerResponseModel();

        List<SimulationResult> results =
                new ArrayList<>();

        SimulationScenario[] scenarios =
                SimulationScenario.values();

        for (int i = 0; i < count; i++) {

            SimulationScenario scenario =
                    scenarios[
                            random.nextInt(
                                    scenarios.length
                            )
                    ];

            BigDecimal invoiceAmount =
                    generateInvoiceAmount(random);

            String customerReference =
                    String.format(
                            "SIM-CUSTOMER-%04d",
                            i + 1
                    );

            String invoiceReference =
                    String.format(
                            "SIM-INVOICE-%04d",
                            i + 1
                    );

            long scenarioSeed =
                    random.nextLong();

            SimulationResult result =
                    responseModel.simulate(
                            scenarioSeed,
                            scenario,
                            customerReference,
                            invoiceReference,
                            invoiceAmount,
                            clock
                    );

            results.add(result);

            clock.advanceDays(1);
        }

        return Collections.unmodifiableList(results);
    }

    private BigDecimal generateInvoiceAmount(
            Random random
    ) {

        double value =
                MIN_INVOICE.doubleValue()
                        + random.nextDouble()
                        * (
                        MAX_INVOICE.doubleValue()
                                - MIN_INVOICE.doubleValue()
                );

        return BigDecimal.valueOf(value)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }
}