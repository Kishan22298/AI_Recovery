package com.recovery.ReceivablesGuard.api;

import com.recovery.ReceivablesGuard.simulation.SimulationResult;
import com.recovery.ReceivablesGuard.simulation.SimulationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import com.recovery.ReceivablesGuard.TestcontainersConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class SimulationControllerTest {

    @Autowired
    private SimulationService simulationService;

    @Test
    void sameSeedProducesSameSimulation() {

        List<SimulationResult> first =
                simulationService.generateSimulation(42L);

        List<SimulationResult> second =
                simulationService.generateSimulation(42L);

        assertEquals(first, second);
    }

    @Test
    void differentSeedsMayProduceDifferentSimulation() {

        List<SimulationResult> first =
                simulationService.generateSimulation(42L);

        List<SimulationResult> second =
                simulationService.generateSimulation(43L);

        assertNotEquals(first, second);
    }
}