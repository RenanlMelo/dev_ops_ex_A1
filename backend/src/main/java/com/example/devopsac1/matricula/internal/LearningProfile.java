package com.example.devopsac1.matricula.internal;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/** Identidade persistente do laboratório local; não representa autenticação. */
@Entity
public class LearningProfile {
    @Id private String id;
    private Long studentId;
    protected LearningProfile() {}
    public LearningProfile(String id, Long studentId) { this.id = id; this.studentId = studentId; }
    public Long getStudentId() { return studentId; }
}
