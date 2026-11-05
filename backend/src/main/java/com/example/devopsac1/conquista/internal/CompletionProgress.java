package com.example.devopsac1.conquista.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "completion_progress", indexes = @Index(name = "ix_progress_student", columnList = "student_id"))
public class CompletionProgress {
    @Id
    private Long enrollmentId;
    @Column(name = "student_id")
    private Long studentId;
    private double grade;
    private int points;
    private boolean approved;
    protected CompletionProgress() { }
    public CompletionProgress(Long enrollmentId, Long studentId, double grade) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.grade = grade;
        this.points = GamificationRules.points(grade);
        this.approved = GamificationRules.approved(grade);
    }
    public Long getStudentId() { return studentId; }
}
