package com.example.devopsac1.recompensa.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RedemptionRepository extends JpaRepository<Redemption, String> {
    List<Redemption> findTop20ByStudentIdOrderByRedeemedAtDescIdAsc(Long studentId);
}
