package com.example.devopsac1.carteira.api;

import java.time.Instant;
import java.util.List;

public record WalletView(Long studentId, long balance, List<Entry> entries) {
    public record Entry(String reference, long amount, String description, Instant createdAt) { }
}
