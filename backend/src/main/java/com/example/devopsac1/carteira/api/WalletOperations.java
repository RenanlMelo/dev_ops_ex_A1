package com.example.devopsac1.carteira.api;

public interface WalletOperations {
    void creditOnce(Long studentId, String reference, long amount, String description);
    void debitOnce(Long studentId, String reference, long amount, String description);
}
