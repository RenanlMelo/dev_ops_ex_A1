package com.example.devopsac1.Dto;

import com.example.devopsac1.Domain.Student;

public record StudentResponse(Long id, String name) {

    public static StudentResponse from(Student student) {
        return new StudentResponse(student.getId(), student.getName());
    }
}
