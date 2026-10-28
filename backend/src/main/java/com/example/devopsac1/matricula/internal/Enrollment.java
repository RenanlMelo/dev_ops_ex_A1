package com.example.devopsac1.matricula.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Enrollment {
    private static final double MINIMUM_GRADE_FOR_EXTRA_COURSES = 7.0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The existing FK columns remain unchanged; scalar IDs avoid cross-module
    // JPA entity dependencies. Lookup contracts validate both references.
    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "student_id")
    private Long studentId;

    private boolean completed;
    private Double grade;

    protected Enrollment() {}

    public Enrollment(Long courseId, Long studentId) {
        this.courseId = courseId;
        this.studentId = studentId;
    }

    public void complete(double grade) {
        this.completed = true;
        this.grade = grade;
    }

    public Long getId() { return id; }
    public Long getCourseId() { return courseId; }
    public Long getStudentId() { return studentId; }
    public boolean isCompleted() { return completed; }
    public Double getGrade() { return grade; }

    public double getAverage() {
        if (!completed) {
            throw new IllegalStateException("Average unavailable: the course has not been completed yet.");
        }
        return grade;
    }

    public boolean hasGradeAboveMinimum() {
        return completed && grade > MINIMUM_GRADE_FOR_EXTRA_COURSES;
    }
}
