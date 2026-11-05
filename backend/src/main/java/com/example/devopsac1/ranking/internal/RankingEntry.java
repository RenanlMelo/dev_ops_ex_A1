package com.example.devopsac1.ranking.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "ranking_entry", indexes = @Index(name = "ix_ranking_order", columnList = "points DESC, student_id ASC"))
public class RankingEntry {
    @Id
    @Column(name = "student_id")
    private Long studentId;
    private long points;
    private int completedCourses;
    private int approvedCourses;
    protected RankingEntry() { }
    public RankingEntry(Long studentId, long points, int completedCourses, int approvedCourses) {
        this.studentId = studentId;
        this.points = points;
        this.completedCourses = completedCourses;
        this.approvedCourses = approvedCourses;
    }
    public Long getStudentId() { return studentId; }
    public long getPoints() { return points; }
    public int getCompletedCourses() { return completedCourses; }
    public int getApprovedCourses() { return approvedCourses; }
}
