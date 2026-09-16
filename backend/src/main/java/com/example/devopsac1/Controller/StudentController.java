package com.example.devopsac1.Controller;

import com.example.devopsac1.Domain.Student;
import com.example.devopsac1.Dto.EligibilityResponse;
import com.example.devopsac1.Dto.EnrollmentResponse;
import com.example.devopsac1.Dto.StudentRequest;
import com.example.devopsac1.Dto.StudentResponse;
import com.example.devopsac1.Exception.ResourceNotFoundException;
import com.example.devopsac1.Repository.EnrollmentRepository;
import com.example.devopsac1.Repository.StudentRepository;
import com.example.devopsac1.Service.CourseService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseService courseService;

    public StudentController(StudentRepository studentRepository, EnrollmentRepository enrollmentRepository,
            CourseService courseService) {
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseService = courseService;
    }

    @GetMapping
    public Page<StudentResponse> list(Pageable pageable) {
        return studentRepository.findAll(pageable).map(StudentResponse::from);
    }

    @GetMapping("/{id}")
    public StudentResponse get(@PathVariable Long id) {
        return StudentResponse.from(findOrThrow(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentResponse create(@Valid @RequestBody StudentRequest request) {
        return StudentResponse.from(studentRepository.save(new Student(request.name())));
    }

    @PutMapping("/{id}")
    public StudentResponse update(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        Student student = findOrThrow(id);
        student.rename(request.name());
        return StudentResponse.from(studentRepository.save(student));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student " + id + " not found");
        }
        studentRepository.deleteById(id);
    }

    @GetMapping("/{id}/enrollments")
    public List<EnrollmentResponse> enrollments(@PathVariable Long id) {
        findOrThrow(id);
        return enrollmentRepository.findByStudentId(id).stream()
                .map(EnrollmentResponse::from)
                .toList();
    }

    @GetMapping("/{id}/eligible-for-extra-courses")
    public EligibilityResponse eligibility(@PathVariable Long id) {
        findOrThrow(id);
        boolean eligible = enrollmentRepository.findByStudentId(id).stream()
                .anyMatch(courseService::isEligibleForExtraCourses);
        return new EligibilityResponse(id, eligible);
    }

    private Student findOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student " + id + " not found"));
    }
}
