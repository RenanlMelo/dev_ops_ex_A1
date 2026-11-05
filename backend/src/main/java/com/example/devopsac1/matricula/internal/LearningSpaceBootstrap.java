package com.example.devopsac1.matricula.internal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true", matchIfMissing = true)
public class LearningSpaceBootstrap implements ApplicationRunner {
    private final LearningSpaceService service;
    private final String name;
    public LearningSpaceBootstrap(LearningSpaceService service, @Value("${app.demo.student-name:Renan}") String name) {
        this.service = service; this.name = name;
    }
    @Override
    public void run(ApplicationArguments args) { service.initialize(name); }
}
