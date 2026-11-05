package com.example.devopsac1.conquista.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProgressRepository extends JpaRepository<CompletionProgress, Long> {
    long countByStudentId(Long studentId);
    long countByStudentIdAndApprovedTrue(Long studentId);
    boolean existsByStudentIdAndGrade(Long studentId, double grade);
    @Query("select coalesce(sum(p.points), 0) from CompletionProgress p where p.studentId = :studentId")
    long points(Long studentId);
}
