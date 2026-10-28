package com.example.devopsac1.matricula.api;

import jakarta.validation.constraints.NotNull;

public record CreateEnrollmentRequest(@NotNull Long courseId, @NotNull Long studentId) {
}
