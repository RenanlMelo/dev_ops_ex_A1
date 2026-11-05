package com.example.devopsac1.ranking.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RankingController {
    private final RankingQuery query;
    public RankingController(RankingQuery query) { this.query = query; }
    @GetMapping("/ranking")
    public RankingPage list(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "20") int size) {
        return query.list(page, size);
    }
    @GetMapping("/ranking/students/{studentId}")
    public RankingRow student(@PathVariable Long studentId) { return query.student(studentId); }
}
