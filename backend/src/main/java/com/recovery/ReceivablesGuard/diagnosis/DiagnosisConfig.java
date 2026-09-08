package com.recovery.ReceivablesGuard.diagnosis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class DiagnosisConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}