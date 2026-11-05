package com.example.devopsac1.ranking.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RankingRepository extends JpaRepository<RankingEntry, Long> {
    @Query("select count(r) from RankingEntry r where r.points > :points "
            + "or (r.points = :points and r.studentId < :studentId)")
    long preceding(long points, Long studentId);
}
