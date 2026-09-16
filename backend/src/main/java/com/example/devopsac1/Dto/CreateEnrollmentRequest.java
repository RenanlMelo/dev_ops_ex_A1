package com.example.devopsac1.Dto;

import jakarta.validation.constraints.NotNull;

public record CreateEnrollmentRequest(@NotNull Long courseId, @NotNull Long studentId) {
}
