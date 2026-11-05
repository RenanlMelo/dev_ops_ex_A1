package com.example.devopsac1.recompensa.api;

import java.time.Instant;

public record RedemptionView(String requestId, Long studentId, String rewardCode,
                             String rewardName, long cost, Instant redeemedAt) { }
