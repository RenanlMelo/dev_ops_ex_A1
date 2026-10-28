package com.example.devopsac1.matricula.api;

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
    private final EnrollmentCommand service;

    public EnrollmentController(EnrollmentCommand service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentResponse create(@Valid @RequestBody CreateEnrollmentRequest request) {
        return service.create(request.courseId(), request.studentId());
    }

    @PostMapping("/{id}/complete")
    public EnrollmentResponse complete(@PathVariable Long id, @Valid @RequestBody CompleteEnrollmentRequest request) {
        return service.complete(id, request.grade());
    }
}
