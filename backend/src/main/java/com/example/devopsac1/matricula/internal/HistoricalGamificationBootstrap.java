package com.example.devopsac1.matricula.internal;

import com.example.devopsac1.conquista.api.CompletionAwardCommand;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/** Replays legacy completed enrollments using the same idempotent contract. */
@Component
@ConditionalOnProperty(name = "gamification.backfill.enabled", havingValue = "true", matchIfMissing = true)
public class HistoricalGamificationBootstrap implements ApplicationRunner {
    private final EnrollmentRepository enrollments;
    private final CompletionAwardCommand awards;
    public HistoricalGamificationBootstrap(EnrollmentRepository enrollments, CompletionAwardCommand awards) {
        this.enrollments = enrollments;
        this.awards = awards;
    }
    @Override
    public void run(ApplicationArguments args) {
        int page = 0;
        org.springframework.data.domain.Page<Enrollment> batch;
        do {
            batch = enrollments.findByCompletedTrue(PageRequest.of(page++, 100, Sort.by("id")));
            batch.forEach(e -> awards.recordCompletion(e.getId(), e.getStudentId(), e.getCourseId(), e.getGrade()));
        } while (batch.hasNext());
    }
}
