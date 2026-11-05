package com.example.devopsac1.ranking.api;

public record RankingRow(Long studentId, String studentName, long points,
                         int completedCourses, int approvedCourses, Long position) { }
