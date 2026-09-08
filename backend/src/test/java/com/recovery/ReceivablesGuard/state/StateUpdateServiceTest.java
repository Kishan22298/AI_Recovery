package com.recovery.ReceivablesGuard.state;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import com.recovery.ReceivablesGuard.TestcontainersConfiguration;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class StateUpdateServiceTest {

    @Autowired
    private StateUpdateService stateUpdateService;

    @Test
    void partialPaymentProducesUpdatedState() {

        StateUpdateResult result =
                stateUpdateService.applyPayment(
                        1L,
                        new BigDecimal("100000"),
                        new BigDecimal("25000"),
                        "OUTSTANDING"
                );

        assertEquals(
                new BigDecimal("100000"),
                result.previousOutstandingAmount()
        );

        assertEquals(
                new BigDecimal("75000"),
                result.newOutstandingAmount()
        );

        assertEquals(
                "OUTSTANDING",
                result.newInvoiceStatus()
        );

        assertTrue(result.stateChanged());
    }

    @Test
    void fullPaymentProducesPaidState() {

        StateUpdateResult result =
                stateUpdateService.applyPayment(
                        1L,
                        new BigDecimal("100000"),
                        new BigDecimal("100000"),
                        "OUTSTANDING"
                );

        assertEquals(
                new BigDecimal("0"),
                result.newOutstandingAmount()
        );

        assertEquals(
                "PAID",
                result.newInvoiceStatus()
        );
    }
}