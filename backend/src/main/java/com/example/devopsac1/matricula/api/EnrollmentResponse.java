package com.example.devopsac1.matricula.api;

public record EnrollmentResponse(
        Long id, Long courseId, String courseName, Long studentId,
        String studentName, boolean completed, Double grade
) {}
