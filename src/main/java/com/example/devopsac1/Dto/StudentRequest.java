package com.example.devopsac1.Dto;

import jakarta.validation.constraints.NotBlank;

public record StudentRequest(@NotBlank String name) {
}
