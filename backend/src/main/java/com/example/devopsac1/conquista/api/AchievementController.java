package com.example.devopsac1.conquista.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AchievementController {
    private final AchievementQuery query;
    public AchievementController(AchievementQuery query) { this.query = query; }
    @GetMapping("/students/{studentId}/achievements")
    public AchievementView view(@PathVariable Long studentId) { return query.view(studentId); }
}
