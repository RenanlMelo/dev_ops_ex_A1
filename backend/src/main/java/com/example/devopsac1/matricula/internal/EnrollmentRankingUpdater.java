package com.example.devopsac1.matricula.internal;

import com.example.devopsac1.aluno.api.StudentLock;
import com.example.devopsac1.ranking.api.Scoreboard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Adaptador da matrícula para o Ranking enquanto a entrega de conquistas não é integrada. */
@Service
public class EnrollmentRankingUpdater {
    private final EnrollmentRepository repository;
    private final StudentLock lock;
    private final Scoreboard scoreboard;
    public EnrollmentRankingUpdater(EnrollmentRepository repository, StudentLock lock, Scoreboard scoreboard) {
        this.repository = repository; this.lock = lock; this.scoreboard = scoreboard;
    }
    @Transactional
    public void refresh(Long studentId) {
        lock.lock(studentId);
        var completed = repository.findByStudentId(studentId).stream().filter(Enrollment::isCompleted).toList();
        long points = completed.stream().mapToLong(e -> Math.round(e.getGrade() * 10)).sum();
        int approved = (int) completed.stream().filter(Enrollment::hasGradeAboveMinimum).count();
        scoreboard.replace(studentId, points, completed.size(), approved);
    }
}
