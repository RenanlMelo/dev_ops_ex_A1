package com.example.devopsac1.Dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CompleteEnrollmentRequest(
        @NotNull @DecimalMin("0.0") @DecimalMax("10.0") Double grade
) {
}
