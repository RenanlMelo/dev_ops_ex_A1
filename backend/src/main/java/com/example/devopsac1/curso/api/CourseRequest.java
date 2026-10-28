package com.example.devopsac1.curso.api;

import jakarta.validation.constraints.NotBlank;

public record CourseRequest(@NotBlank String name) {
}
