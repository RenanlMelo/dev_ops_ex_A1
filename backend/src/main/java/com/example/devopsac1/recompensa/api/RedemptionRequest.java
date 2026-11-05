package com.example.devopsac1.recompensa.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RedemptionRequest(@NotBlank String rewardCode,
                                @NotBlank @Pattern(regexp = "[A-Za-z0-9:_-]{1,80}") String requestId) { }
