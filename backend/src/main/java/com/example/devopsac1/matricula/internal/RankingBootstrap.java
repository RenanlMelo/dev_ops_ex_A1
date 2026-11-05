package com.example.devopsac1.matricula.internal;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "ranking.backfill.enabled", havingValue = "true", matchIfMissing = true)
public class RankingBootstrap implements ApplicationRunner {
    private final EnrollmentRepository repository;
    private final EnrollmentRankingUpdater updater;
    public RankingBootstrap(EnrollmentRepository repository, EnrollmentRankingUpdater updater) {
        this.repository = repository; this.updater = updater;
    }
    @Override
    public void run(ApplicationArguments args) {
        repository.completedStudentIds().forEach(updater::refresh);
    }
}
