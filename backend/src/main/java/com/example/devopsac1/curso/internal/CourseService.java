package com.example.devopsac1.curso.internal;

import com.example.devopsac1.curso.api.CourseLookup;
import com.example.devopsac1.curso.api.CourseCommand;
import com.example.devopsac1.curso.api.CourseResponse;
import com.example.devopsac1.curso.api.CourseView;
import com.example.devopsac1.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseService implements CourseLookup, CourseCommand {
    private final CourseRepository repository;

    public CourseService(CourseRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CourseResponse create(String name) {
        Course course = repository.save(new Course(name));
        return new CourseResponse(course.getId(), course.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public CourseView require(Long id) {
        Course course = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course " + id + " not found"));
        return new CourseView(course.getId(), course.getName());
    }
}
