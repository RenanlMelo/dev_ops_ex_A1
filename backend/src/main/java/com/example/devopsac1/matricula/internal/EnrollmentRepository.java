package com.example.devopsac1.matricula.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudentId(Long studentId);
    @org.springframework.data.jpa.repository.Query("select distinct e.studentId from Enrollment e where e.completed = true")
    List<Long> completedStudentIds();
}
