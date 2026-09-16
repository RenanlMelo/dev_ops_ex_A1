package com.example.devopsac1.Dto;

import jakarta.validation.constraints.NotBlank;

public record CourseRequest(@NotBlank String name) {
}
