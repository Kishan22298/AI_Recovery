package com.recovery.ReceivablesGuard.simulation;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Deterministic simulation clock.
 *
 * The simulation never uses the actual system clock.
 */
public final class SimulationClock {

    private final Instant startTime;
    private Instant currentTime;

    public SimulationClock() {
        this(Instant.parse("2026-01-01T00:00:00Z"));
    }

    public SimulationClock(Instant startTime) {
        this.startTime = startTime;
        this.currentTime = startTime;
    }

    public Instant now() {
        return currentTime;
    }

    public Instant startTime() {
        return startTime;
    }

    public void advanceDays(long days) {
        currentTime = currentTime.plusSeconds(days * 24L * 60L * 60L);
    }

    public void advanceHours(long hours) {
        currentTime = currentTime.plusSeconds(hours * 60L * 60L);
    }

    public void advanceMinutes(long minutes) {
        currentTime = currentTime.plusSeconds(minutes * 60L);
    }

    public ZonedDateTime nowUtc() {
        return currentTime.atZone(ZoneOffset.UTC);
    }

    public void reset() {
        currentTime = startTime;
    }
}