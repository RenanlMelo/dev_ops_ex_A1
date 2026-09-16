package com.example.devopsac1.Controller;

import com.example.devopsac1.Domain.Course;
import com.example.devopsac1.Domain.Enrollment;
import com.example.devopsac1.Dto.AverageResponse;
import com.example.devopsac1.Dto.CourseRequest;
import com.example.devopsac1.Dto.CourseResponse;
import com.example.devopsac1.Dto.EnrollmentResponse;
import com.example.devopsac1.Exception.ResourceNotFoundException;
import com.example.devopsac1.Repository.CourseRepository;
import com.example.devopsac1.Repository.EnrollmentRepository;
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
@RequestMapping("/courses")
public class CourseController {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public CourseController(CourseRepository courseRepository, EnrollmentRepository enrollmentRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @GetMapping
    public Page<CourseResponse> list(Pageable pageable) {
        return courseRepository.findAll(pageable).map(CourseResponse::from);
    }

    @GetMapping("/{id}")
    public CourseResponse get(@PathVariable Long id) {
        return CourseResponse.from(findOrThrow(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse create(@Valid @RequestBody CourseRequest request) {
        return CourseResponse.from(courseRepository.save(new Course(request.name())));
    }

    @PutMapping("/{id}")
    public CourseResponse update(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        Course course = findOrThrow(id);
        course.rename(request.name());
        return CourseResponse.from(courseRepository.save(course));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course " + id + " not found");
        }
        courseRepository.deleteById(id);
    }

    @GetMapping("/{id}/enrollments")
    public List<EnrollmentResponse> enrollments(@PathVariable Long id) {
        findOrThrow(id);
        return enrollmentRepository.findByCourseId(id).stream()
                .map(EnrollmentResponse::from)
                .toList();
    }

    @GetMapping("/{id}/average")
    public AverageResponse average(@PathVariable Long id) {
        findOrThrow(id);
        double average = enrollmentRepository.findByCourseId(id).stream()
                .filter(Enrollment::isCompleted)
                .mapToDouble(Enrollment::getGrade)
                .average()
                .orElse(0.0);
        return new AverageResponse(average);
    }

    private Course findOrThrow(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course " + id + " not found"));
    }
}
