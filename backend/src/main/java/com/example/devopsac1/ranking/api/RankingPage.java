package com.example.devopsac1.ranking.api;

import java.util.List;

public record RankingPage(List<RankingRow> entries, int page, int size,
                          long totalElements, int totalPages) { }
