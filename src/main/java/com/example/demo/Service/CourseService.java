package com.example.demo.Service;

import com.example.demo.Model.Enrollment;

public class CourseService {

    public void completeCourse(Enrollment enrollment, double grade) {
        enrollment.complete(grade);
    }

    public boolean isEligibleForExtraCourses(Enrollment enrollment) {
        return enrollment.hasGradeAboveMinimum();
    }

    public double getAverage(Enrollment enrollment) {
        if (!enrollment.isCompleted()) {
            throw new IllegalStateException(
                    "Average unavailable: the course has not been completed yet.");
        }
        return enrollment.getGrade();
    }
}
