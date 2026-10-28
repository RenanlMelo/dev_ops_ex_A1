package com.example.devopsac1.aluno.api;

import jakarta.validation.constraints.NotBlank;

public record StudentRequest(@NotBlank String name) {
}
