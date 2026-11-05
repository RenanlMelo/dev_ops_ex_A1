package com.example.devopsac1.conquista.internal;

import com.example.devopsac1.aluno.api.StudentLock;
import com.example.devopsac1.aluno.api.StudentLookup;
import com.example.devopsac1.carteira.api.WalletOperations;
import com.example.devopsac1.conquista.api.AchievementQuery;
import com.example.devopsac1.conquista.api.AchievementView;
import com.example.devopsac1.conquista.api.CompletionAwardCommand;
import com.example.devopsac1.ranking.api.Scoreboard;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AchievementService implements CompletionAwardCommand, AchievementQuery {
    private final ProgressRepository progress;
    private final BadgeRepository badges;
    private final StudentLock lock;
    private final StudentLookup students;
    private final WalletOperations wallet;
    private final Scoreboard scoreboard;
    public AchievementService(ProgressRepository progress, BadgeRepository badges, StudentLock lock,
                              StudentLookup students, WalletOperations wallet, Scoreboard scoreboard) {
        this.progress = progress;
        this.badges = badges;
        this.lock = lock;
        this.students = students;
        this.wallet = wallet;
        this.scoreboard = scoreboard;
    }

    @Override
    @Transactional
    public void recordCompletion(Long enrollmentId, Long studentId, Long courseId, double grade) {
        GamificationRules.points(grade);
        lock.lock(studentId);
        var old = progress.findById(enrollmentId).orElse(null);
        if (old != null && !old.getStudentId().equals(studentId)) {
            throw new IllegalStateException("Matrícula pertence a outro aluno");
        }
        progress.saveAndFlush(new CompletionProgress(enrollmentId, studentId, grade));
        if (GamificationRules.approved(grade)) {
            wallet.creditOnce(studentId, "approval:" + studentId + ":" + courseId, GamificationRules.APPROVAL_COINS,
                    "Aprovação na matrícula " + enrollmentId);
        }
        long approved = progress.countByStudentIdAndApprovedTrue(studentId);
        boolean perfect = progress.existsByStudentIdAndGrade(studentId, 10);
        for (var rule : GamificationRules.badges()) {
            if (rule.achieved(approved, perfect) && !badges.existsById(studentId + ":" + rule.code())) {
                badges.save(new StudentBadge(studentId, rule.code()));
            }
        }
        scoreboard.replace(studentId, progress.points(studentId),
                Math.toIntExact(progress.countByStudentId(studentId)), Math.toIntExact(approved));
    }

    @Override
    @Transactional(readOnly = true)
    public AchievementView view(Long studentId) {
        students.require(studentId);
        var earned = badges.findByStudentId(studentId).stream()
                .collect(Collectors.toMap(StudentBadge::getCode, Function.identity()));
        var catalog = GamificationRules.badges().stream().map(rule -> new AchievementView.Badge(
                rule.code(), rule.name(), rule.description(),
                earned.containsKey(rule.code()) ? earned.get(rule.code()).getEarnedAt() : null)).toList();
        return new AchievementView(studentId, progress.points(studentId),
                Math.toIntExact(progress.countByStudentId(studentId)),
                Math.toIntExact(progress.countByStudentIdAndApprovedTrue(studentId)), catalog);
    }
}
