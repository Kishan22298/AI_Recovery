package com.recovery.ReceivablesGuard.observation;

import org.springframework.stereotype.Service;

/**
 * Application-facing observation service.
 *
 * Later phases can call this service instead of directly
 * depending on SignalAssembler.
 */
@Service
public class ObservationService {

    private final SignalAssembler signalAssembler;

    public ObservationService(SignalAssembler signalAssembler) {
        this.signalAssembler = signalAssembler;
    }

    public InvoiceContext observe(ObservationInput input) {
        return signalAssembler.assemble(input);
    }
}