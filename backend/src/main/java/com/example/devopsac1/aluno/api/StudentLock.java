package com.example.devopsac1.aluno.api;

/** Serializes mutations for one learner inside the caller's transaction. */
public interface StudentLock {
    void lock(Long studentId);
}
