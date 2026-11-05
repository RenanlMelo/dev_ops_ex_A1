package com.example.devopsac1.conquista.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeRepository extends JpaRepository<StudentBadge, String> {
    List<StudentBadge> findByStudentId(Long studentId);
}
