package com.example.devopsac1.Dto;

import com.example.devopsac1.Domain.Course;

public record CourseResponse(Long id, String name) {

    public static CourseResponse from(Course course) {
        return new CourseResponse(course.getId(), course.getName());
    }
}
