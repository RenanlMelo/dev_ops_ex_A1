package com.example.devopsac1.recompensa.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "reward_redemption", indexes = @Index(name = "ix_redemption_student", columnList = "student_id,redeemed_at"))
public class Redemption {
    @Id
    private String id;
    private String requestId;
    @Column(name = "student_id")
    private Long studentId;
    private String rewardCode;
    private String rewardName;
    private long cost;
    @Column(name = "redeemed_at")
    private Instant redeemedAt;
    protected Redemption() { }
    public Redemption(Long studentId, String requestId, String rewardCode, String rewardName, long cost) {
        this.id = studentId + ":" + requestId;
        this.studentId = studentId;
        this.requestId = requestId;
        this.rewardCode = rewardCode;
        this.rewardName = rewardName;
        this.cost = cost;
        this.redeemedAt = Instant.now();
    }
    public String getRequestId() { return requestId; }
    public Long getStudentId() { return studentId; }
    public String getRewardCode() { return rewardCode; }
    public String getRewardName() { return rewardName; }
    public long getCost() { return cost; }
    public Instant getRedeemedAt() { return redeemedAt; }
}
