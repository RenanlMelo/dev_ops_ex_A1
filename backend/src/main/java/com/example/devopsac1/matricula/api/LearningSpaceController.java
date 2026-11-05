package com.example.devopsac1.matricula.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LearningSpaceController {
    private final LearningSpaceQuery query;
    public LearningSpaceController(LearningSpaceQuery query) { this.query = query; }
    @GetMapping("/learning-space")
    public LearningSpace view() { return query.view(); }
}
