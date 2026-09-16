package com.example.devopsac1.Service;

import com.example.devopsac1.Domain.Enrollment;

public class CourseService {

    public void completeCourse(Enrollment enrollment, double grade) {
        enrollment.complete(grade);
    }

    public boolean isEligibleForExtraCourses(Enrollment enrollment) {
        return enrollment.hasGradeAboveMinimum();
    }

    public double getAverage(Enrollment enrollment) {
        requireCompletedCourse(enrollment);
        return enrollment.getGrade();
    }

    private void requireCompletedCourse(Enrollment enrollment) {
        if (!enrollment.isCompleted()) {
            throw new IllegalStateException(
                    "Average unavailable: the course has not been completed yet.");
        }
    }
}
