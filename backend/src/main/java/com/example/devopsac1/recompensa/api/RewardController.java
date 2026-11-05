package com.example.devopsac1.recompensa.api;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RewardController {
    private final RewardQuery query;
    private final RewardCommand command;
    public RewardController(RewardQuery query, RewardCommand command) {
        this.query = query;
        this.command = command;
    }
    @GetMapping("/rewards")
    public List<RewardView> catalog() { return query.catalog(); }
    @GetMapping("/students/{studentId}/redemptions")
    public List<RedemptionView> redemptions(@PathVariable Long studentId) { return query.redemptions(studentId); }
    @PostMapping("/students/{studentId}/redemptions")
    @ResponseStatus(HttpStatus.CREATED)
    public RedemptionView redeem(@PathVariable Long studentId, @Valid @RequestBody RedemptionRequest request) {
        return command.redeem(studentId, request.rewardCode(), request.requestId());
    }
}
