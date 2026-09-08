package com.recovery.ReceivablesGuard.simulation;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimulationClockTest {

    @Test
    void clockStartsAtDeterministicTime() {

        SimulationClock clock =
                new SimulationClock();

        assertEquals(
                Instant.parse(
                        "2026-01-01T00:00:00Z"
                ),
                clock.now()
        );
    }

    @Test
    void clockAdvancesDeterministically() {

        SimulationClock clock =
                new SimulationClock();

        clock.advanceDays(2);

        assertEquals(
                Instant.parse(
                        "2026-01-03T00:00:00Z"
                ),
                clock.now()
        );
    }

    @Test
    void resetReturnsToStart() {

        SimulationClock clock =
                new SimulationClock();

        clock.advanceDays(10);

        clock.reset();

        assertEquals(
                Instant.parse(
                        "2026-01-01T00:00:00Z"
                ),
                clock.now()
        );
    }
}