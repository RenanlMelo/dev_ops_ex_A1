package com.example.devopsac1.carteira.internal;

import com.example.devopsac1.aluno.api.StudentLock;
import com.example.devopsac1.aluno.api.StudentLookup;
import com.example.devopsac1.carteira.api.WalletOperations;
import com.example.devopsac1.carteira.api.WalletQuery;
import com.example.devopsac1.carteira.api.WalletView;
import com.example.devopsac1.shared.CommittedMetrics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService implements WalletOperations, WalletQuery {
    private final WalletEntryRepository repository;
    private final StudentLock lock;
    private final StudentLookup students;
    private final CommittedMetrics metrics;
    public WalletService(WalletEntryRepository repository, StudentLock lock,
                         StudentLookup students, CommittedMetrics metrics) {
        this.repository = repository;
        this.lock = lock;
        this.students = students;
        this.metrics = metrics;
    }

    @Override
    @Transactional
    public void creditOnce(Long studentId, String reference, long amount, String description) {
        requirePositive(amount);
        if (adjust(studentId, reference, amount, description)) {
            metrics.increment("gamificacao.moedas.creditadas", amount);
        }
    }

    @Override
    @Transactional
    public void debitOnce(Long studentId, String reference, long amount, String description) {
        requirePositive(amount);
        adjust(studentId, reference, -amount, description);
    }

    private void requirePositive(long amount) {
        if (amount <= 0) { throw new IllegalArgumentException("Coin amount must be positive"); }
    }

    private boolean adjust(Long studentId, String reference, long delta, String description) {
        lock.lock(studentId);
        WalletEntry previous = repository.findById(reference).orElse(null);
        if (previous != null) {
            if (!previous.getStudentId().equals(studentId) || previous.getAmount() != delta) {
                throw new IllegalStateException("Referência de operação já utilizada");
            }
            return false;
        }
        if (repository.balance(studentId) + delta < 0) {
            throw new IllegalStateException("Moedas insuficientes para esta recompensa");
        }
        repository.save(new WalletEntry(reference, studentId, delta, description));
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public WalletView view(Long studentId) {
        students.require(studentId);
        return new WalletView(studentId, repository.balance(studentId),
                repository.findTop20ByStudentIdOrderByCreatedAtDescReferenceAsc(studentId).stream()
                        .map(e -> new WalletView.Entry(e.getReference(), e.getAmount(),
                                e.getDescription(), e.getCreatedAt())).toList());
    }
}
