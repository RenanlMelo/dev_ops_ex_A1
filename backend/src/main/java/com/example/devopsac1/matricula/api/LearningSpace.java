package com.example.devopsac1.matricula.api;

import com.example.devopsac1.aluno.api.StudentView;
import java.util.List;

public record LearningSpace(StudentView student, List<Course> courses) {
    public record Course(Long id, Long enrollmentId, String name, boolean completed, Double grade) {}
}
