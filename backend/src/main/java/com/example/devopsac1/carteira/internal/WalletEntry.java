package com.example.devopsac1.carteira.internal;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "wallet_entry", indexes = @Index(name = "ix_wallet_student", columnList = "student_id,created_at"))
public class WalletEntry {
    @Id
    private String reference;
    @Column(name = "student_id")
    private Long studentId;
    private long amount;
    private String description;
    @Column(name = "created_at")
    private Instant createdAt;
    protected WalletEntry() { }
    public WalletEntry(String reference, Long studentId, long amount, String description) {
        this.reference = reference;
        this.studentId = studentId;
        this.amount = amount;
        this.description = description;
        this.createdAt = Instant.now();
    }
    public String getReference() { return reference; }
    public Long getStudentId() { return studentId; }
    public long getAmount() { return amount; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
}
