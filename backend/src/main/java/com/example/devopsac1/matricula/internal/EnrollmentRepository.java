package com.example.devopsac1.matricula.internal;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select e from Enrollment e where e.id = :id")
    Optional<Enrollment> lockById(@org.springframework.data.repository.query.Param("id") Long id);

    List<Enrollment> findByStudentId(Long studentId);
    Page<Enrollment> findByCompletedTrue(Pageable page);
    Optional<Enrollment> findFirstByCourseIdAndStudentIdOrderByIdAsc(Long courseId, Long studentId);
}
