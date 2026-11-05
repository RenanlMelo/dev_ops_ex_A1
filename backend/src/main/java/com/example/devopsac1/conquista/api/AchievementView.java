package com.example.devopsac1.conquista.api;

import java.time.Instant;
import java.util.List;

public record AchievementView(Long studentId, long points, int completedCourses,
                              int approvedCourses, List<Badge> badges) {
    public record Badge(String code, String name, String description, Instant earnedAt) { }
}
