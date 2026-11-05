package com.example.devopsac1.recompensa.api;

import java.util.List;

public interface RewardQuery {
    List<RewardView> catalog();
    List<RedemptionView> redemptions(Long studentId);
}
