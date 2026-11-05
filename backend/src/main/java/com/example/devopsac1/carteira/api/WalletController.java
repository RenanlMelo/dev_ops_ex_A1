package com.example.devopsac1.carteira.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WalletController {
    private final WalletQuery query;
    public WalletController(WalletQuery query) { this.query = query; }
    @GetMapping("/students/{studentId}/wallet")
    public WalletView view(@PathVariable Long studentId) { return query.view(studentId); }
}
