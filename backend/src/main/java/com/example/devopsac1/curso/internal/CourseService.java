package com.example.devopsac1.curso.internal;

import com.example.devopsac1.curso.api.CourseLookup;
import com.example.devopsac1.curso.api.CourseCommand;
import com.example.devopsac1.curso.api.CourseResponse;
import com.example.devopsac1.curso.api.CourseView;
import com.example.devopsac1.curso.api.CourseCatalog;
import java.util.List;
import java.util.Collection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.example.devopsac1.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseService implements CourseLookup, CourseCommand, CourseCatalog {
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

    @Override
    @Transactional(readOnly = true)
    public List<CourseView> list(int limit) {
        return repository.findAll(PageRequest.of(0, Math.clamp(limit, 1, 100), Sort.by("name")))
                .stream().map(c -> new CourseView(c.getId(), c.getName())).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseView> findAll(Collection<Long> ids) {
        return repository.findAllById(ids).stream().map(c -> new CourseView(c.getId(), c.getName())).toList();
    }
}
