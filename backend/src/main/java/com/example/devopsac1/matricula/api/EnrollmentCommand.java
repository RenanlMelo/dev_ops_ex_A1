package com.example.devopsac1.matricula.api;

public interface EnrollmentCommand {
    EnrollmentResponse create(Long courseId, Long studentId);
    EnrollmentResponse complete(Long id, double grade);
}
