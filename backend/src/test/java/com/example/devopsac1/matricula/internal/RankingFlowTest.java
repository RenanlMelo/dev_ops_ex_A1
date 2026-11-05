package com.example.devopsac1.matricula.internal;

import static org.junit.jupiter.api.Assertions.*;
import com.example.devopsac1.aluno.api.StudentCommand;
import com.example.devopsac1.curso.api.CourseCommand;
import com.example.devopsac1.matricula.api.EnrollmentCommand;
import com.example.devopsac1.ranking.api.RankingQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class RankingFlowTest {
    @Autowired StudentCommand students;
    @Autowired CourseCommand courses;
    @Autowired EnrollmentCommand enrollments;
    @Autowired RankingQuery ranking;

    @Test
    void completionUpdatesRankingAndGradeRevisionReplacesPoints() {
        long student = students.create("Aluno Ranking").id();
        long course = courses.create("Curso Ranking").id();
        long enrollment = enrollments.create(course, student).id();
        enrollments.complete(enrollment, 8.5);
        assertEquals(85, ranking.student(student).points());
        enrollments.complete(enrollment, 10);
        assertEquals(100, ranking.student(student).points());
        assertEquals(1, ranking.student(student).completedCourses());
    }

    @Test
    void sumsCoursesAndBreaksTiesByStudentId() {
        long first = students.create("Primeiro Ranking").id();
        long second = students.create("Segundo Ranking").id();
        long course = courses.create("Empate").id();
        enrollments.complete(enrollments.create(course, first).id(), 8);
        enrollments.complete(enrollments.create(course, second).id(), 8);
        assertTrue(ranking.student(first).position() < ranking.student(second).position());
        long extra = courses.create("Mais um curso").id();
        enrollments.complete(enrollments.create(extra, second).id(), 9);
        assertEquals(170, ranking.student(second).points());
        assertTrue(ranking.student(second).position() < ranking.student(first).position());
        assertEquals(second, ranking.list(0, 1).entries().getFirst().studentId());
    }
}
