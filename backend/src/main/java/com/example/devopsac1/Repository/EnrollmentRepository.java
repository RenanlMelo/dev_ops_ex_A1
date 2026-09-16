package com.example.devopsac1.Repository;

import com.example.devopsac1.Domain.Enrollment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByCourseId(Long courseId);

    List<Enrollment> findByStudentId(Long studentId);
}
