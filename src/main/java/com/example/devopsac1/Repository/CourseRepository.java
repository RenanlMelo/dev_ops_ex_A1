package com.example.devopsac1.Repository;

import com.example.devopsac1.Domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
