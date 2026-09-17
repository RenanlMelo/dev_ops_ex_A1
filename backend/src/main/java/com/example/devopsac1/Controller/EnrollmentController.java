package com.example.devopsac1.Controller;

import com.example.devopsac1.Domain.Course;
import com.example.devopsac1.Domain.Enrollment;
import com.example.devopsac1.Domain.Student;
import com.example.devopsac1.Dto.CompleteEnrollmentRequest;
import com.example.devopsac1.Dto.CreateEnrollmentRequest;
import com.example.devopsac1.Dto.EnrollmentResponse;
import com.example.devopsac1.Exception.ResourceNotFoundException;
import com.example.devopsac1.Repository.CourseRepository;
import com.example.devopsac1.Repository.EnrollmentRepository;
import com.example.devopsac1.Repository.StudentRepository;
import com.example.devopsac1.Service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final CourseService courseService;

    public EnrollmentController(EnrollmentRepository enrollmentRepository, CourseRepository courseRepository,
            StudentRepository studentRepository, CourseService courseService) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.courseService = courseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentResponse create(@Valid @RequestBody CreateEnrollmentRequest request) {
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course " + request.courseId() + " not found"));
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student " + request.studentId() + " not found"));
        Enrollment enrollment = enrollmentRepository.save(new Enrollment(course, student));
        return EnrollmentResponse.from(enrollment);
    }

    @PostMapping("/{id}/complete")
    public EnrollmentResponse complete(@PathVariable Long id, @Valid @RequestBody CompleteEnrollmentRequest request) {
        Enrollment enrollment = findOrThrow(id);
        courseService.completeCourse(enrollment, request.grade());
        return EnrollmentResponse.from(enrollmentRepository.save(enrollment));
    }

    private Enrollment findOrThrow(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment " + id + " not found"));
    }
}
