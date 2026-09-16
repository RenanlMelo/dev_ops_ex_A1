package com.example.devopsac1.Repository;

import com.example.devopsac1.Domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
