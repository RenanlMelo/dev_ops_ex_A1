package com.example.devopsac1.recompensa.api;

public interface RewardCommand {
    RedemptionView redeem(Long studentId, String rewardCode, String requestId);
}
