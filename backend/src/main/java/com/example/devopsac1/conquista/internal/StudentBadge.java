package com.example.devopsac1.conquista.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "student_badge", indexes = @Index(name = "ix_badge_student", columnList = "student_id"))
public class StudentBadge {
    @Id
    private String id;
    @Column(name = "student_id")
    private Long studentId;
    private String code;
    private Instant earnedAt;
    protected StudentBadge() { }
    public StudentBadge(Long studentId, String code) {
        this.id = studentId + ":" + code;
        this.studentId = studentId;
        this.code = code;
        this.earnedAt = Instant.now();
    }
    public String getCode() { return code; }
    public Instant getEarnedAt() { return earnedAt; }
}
