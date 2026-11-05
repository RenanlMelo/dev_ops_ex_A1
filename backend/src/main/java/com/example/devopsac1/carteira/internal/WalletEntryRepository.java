package com.example.devopsac1.carteira.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WalletEntryRepository extends JpaRepository<WalletEntry, String> {
    @Query("select coalesce(sum(e.amount), 0) from WalletEntry e where e.studentId = :studentId")
    long balance(Long studentId);
    List<WalletEntry> findTop20ByStudentIdOrderByCreatedAtDescReferenceAsc(Long studentId);
}
