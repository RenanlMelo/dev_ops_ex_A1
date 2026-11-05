package com.example.devopsac1.ranking.api;

public interface Scoreboard {
    void replace(Long studentId, long points, int completedCourses, int approvedCourses);
}
