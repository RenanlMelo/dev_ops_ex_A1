package com.example.devopsac1.conquista.api;

public interface CompletionAwardCommand {
    void recordCompletion(Long enrollmentId, Long studentId, Long courseId, double grade);
}
