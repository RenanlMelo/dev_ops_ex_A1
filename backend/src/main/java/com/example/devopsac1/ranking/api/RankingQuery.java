package com.example.devopsac1.ranking.api;

public interface RankingQuery {
    RankingPage list(int page, int size);
    RankingRow student(Long studentId);
}
