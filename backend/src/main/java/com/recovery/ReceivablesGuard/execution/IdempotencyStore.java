package com.recovery.ReceivablesGuard.execution;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class IdempotencyStore {

    private final Set<String> processed =
            ConcurrentHashMap.newKeySet();

    public boolean hasProcessed(
            String executionId
    ) {

        return processed.contains(executionId);
    }

    public boolean markProcessed(
            String executionId
    ) {

        return processed.add(executionId);
    }

    public void clear() {

        processed.clear();
    }
}