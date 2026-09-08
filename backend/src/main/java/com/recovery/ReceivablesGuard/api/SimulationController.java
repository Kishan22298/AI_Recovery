package com.recovery.ReceivablesGuard.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.recovery.ReceivablesGuard.simulation.SimulationResult;
import com.recovery.ReceivablesGuard.simulation.SimulationService;

@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @GetMapping
    public List<SimulationResult> generate(
            @RequestParam(defaultValue = "42") long seed) {

        return simulationService.generate(seed);
    }
}

