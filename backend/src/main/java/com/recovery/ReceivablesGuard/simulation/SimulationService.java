package com.recovery.ReceivablesGuard.simulation;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class SimulationService {

    private final SyntheticDataGenerator syntheticDataGenerator;

    public SimulationService(SyntheticDataGenerator syntheticDataGenerator) {
        this.syntheticDataGenerator = syntheticDataGenerator;
    }

    public List<SimulationResult> generate(long seed) {
        return syntheticDataGenerator.generate(seed, 100);
    }

    public List<SimulationResult> generateSimulation(long seed) {
        return generate(seed);
    }
}

