package com.example.devopsac1.shared;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class CommittedMetrics {
    private final MeterRegistry registry;
    public CommittedMetrics(MeterRegistry registry) { this.registry = registry; }

    public void increment(String name, double amount) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("A metric mutation requires a transaction");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() { registry.counter(name).increment(amount); }
        });
    }
}
