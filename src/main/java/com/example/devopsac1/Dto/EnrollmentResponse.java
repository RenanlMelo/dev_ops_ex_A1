package com.example.devopsac1.Dto;

import com.example.devopsac1.Domain.Enrollment;

public record EnrollmentResponse(
        Long id,
        Long courseId,
        String courseName,
        Long studentId,
        String studentName,
        boolean completed,
        Double grade
) {

    public static EnrollmentResponse from(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getName(),
                enrollment.getStudent().getId(),
                enrollment.getStudent().getName(),
                enrollment.isCompleted(),
                enrollment.getGrade()
        );
    }
}
