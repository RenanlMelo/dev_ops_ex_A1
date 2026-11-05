package com.example.devopsac1.ranking.internal;

import com.example.devopsac1.aluno.api.StudentDirectory;
import com.example.devopsac1.aluno.api.StudentLookup;
import com.example.devopsac1.aluno.api.StudentView;
import com.example.devopsac1.ranking.api.RankingPage;
import com.example.devopsac1.ranking.api.RankingQuery;
import com.example.devopsac1.ranking.api.RankingRow;
import com.example.devopsac1.ranking.api.Scoreboard;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.ArrayList;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RankingService implements RankingQuery, Scoreboard {
    private final RankingRepository repository;
    private final StudentDirectory directory;
    private final StudentLookup students;
    private final MeterRegistry metrics;
    public RankingService(RankingRepository repository, StudentDirectory directory,
                          StudentLookup students, MeterRegistry metrics) {
        this.repository = repository;
        this.directory = directory;
        this.students = students;
        this.metrics = metrics;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void replace(Long studentId, long points, int completedCourses, int approvedCourses) {
        repository.save(new RankingEntry(studentId, points, completedCourses, approvedCourses));
    }

    @Override
    @Transactional(readOnly = true)
    public RankingPage list(int page, int size) {
        if (page < 0) { throw new IllegalArgumentException("Página não pode ser negativa"); }
        int bounded = Math.clamp(size, 1, 100);
        var result = repository.findAll(PageRequest.of(page, bounded,
                Sort.by(Sort.Order.desc("points"), Sort.Order.asc("studentId"))));
        var names = directory.names(result.getContent().stream().map(RankingEntry::getStudentId).toList());
        var rows = new ArrayList<RankingRow>();
        long position = (long) page * bounded;
        for (RankingEntry entry : result) {
            rows.add(row(entry, names.get(entry.getStudentId()), ++position));
        }
        metrics.counter("ranking.consultas").increment();
        return new RankingPage(rows, page, bounded, result.getTotalElements(), result.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public RankingRow student(Long studentId) {
        StudentView student = students.require(studentId);
        RankingEntry entry = repository.findById(studentId).orElse(null);
        if (entry == null) { return new RankingRow(studentId, student.name(), 0, 0, 0, null); }
        return row(entry, student.name(), repository.preceding(entry.getPoints(), studentId) + 1);
    }

    private RankingRow row(RankingEntry entry, String name, long position) {
        return new RankingRow(entry.getStudentId(), name, entry.getPoints(),
                entry.getCompletedCourses(), entry.getApprovedCourses(), position);
    }
}
