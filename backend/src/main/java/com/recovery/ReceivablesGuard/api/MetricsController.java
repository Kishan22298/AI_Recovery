package com.recovery.ReceivablesGuard.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recovery.ReceivablesGuard.api.dto.MetricsResponse;
import com.recovery.ReceivablesGuard.metrics.RecoveryMetricsQueryService;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final RecoveryMetricsQueryService
            recoveryMetricsQueryService;

    public MetricsController(
            RecoveryMetricsQueryService
                    recoveryMetricsQueryService) {

        this.recoveryMetricsQueryService =
                recoveryMetricsQueryService;
    }

    @GetMapping
    public ResponseEntity<MetricsResponse> getMetrics() {

        return ResponseEntity.ok(
                MetricsResponse.from(
                        recoveryMetricsQueryService
                                .calculateMetrics()
                )
        );
    }
}