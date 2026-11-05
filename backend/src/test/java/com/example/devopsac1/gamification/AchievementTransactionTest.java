package com.example.devopsac1.gamification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;

import com.example.devopsac1.aluno.api.StudentCommand;
import com.example.devopsac1.carteira.api.WalletQuery;
import com.example.devopsac1.conquista.api.AchievementQuery;
import com.example.devopsac1.curso.api.CourseCommand;
import com.example.devopsac1.matricula.api.EnrollmentCommand;
import com.example.devopsac1.matricula.api.EligibilityQuery;
import com.example.devopsac1.ranking.api.Scoreboard;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.util.AopTestUtils;
import io.micrometer.core.instrument.MeterRegistry;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:gamification-concurrency;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=5000")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AchievementTransactionTest {
    @Autowired StudentCommand students;
    @Autowired CourseCommand courses;
    @Autowired EnrollmentCommand enrollments;
    @Autowired WalletQuery wallet;
    @Autowired AchievementQuery achievements;
    @Autowired EligibilityQuery eligibility;
    @Autowired MeterRegistry metrics;
    @MockitoSpyBean Scoreboard scoreboard;

    @Test
    void concurrentCompletionsAwardCoinsOnlyOnce() throws Exception {
        long student = students.create("Concurrent completion").id();
        long course = courses.create("Concurrent course").id();
        long enrollment = enrollments.create(course, student).id();
        try (var pool = Executors.newFixedThreadPool(2)) {
            var start = new CountDownLatch(1);
            var first = pool.submit(() -> { start.await(); return enrollments.complete(enrollment, 8.5); });
            var second = pool.submit(() -> { start.await(); return enrollments.complete(enrollment, 8.5); });
            start.countDown();
            assertTrue(first.get(10, TimeUnit.SECONDS).completed());
            assertTrue(second.get(10, TimeUnit.SECONDS).completed());
        }
        assertEquals(50, wallet.view(student).balance());
        assertEquals(1, wallet.view(student).entries().size());
        assertEquals(85, achievements.view(student).points());
    }

    @Test
    void failedGamificationRollsBackCompletionCoinsAndBadges() {
        long student = students.create("Rollback student").id();
        long course = courses.create("Rollback course").id();
        long enrollment = enrollments.create(course, student).id();
        var counter = metrics.find("gamificacao.moedas.creditadas").counter();
        double coinsBefore = counter == null ? 0 : counter.count();
        Scoreboard target = AopTestUtils.getUltimateTargetObject(scoreboard);
        doThrow(new IllegalStateException("Simulated ranking failure")).when(target)
                .replace(eq(student), anyLong(), anyInt(), anyInt());
        assertThrows(IllegalStateException.class, () -> enrollments.complete(enrollment, 8.5));
        assertEquals(0, wallet.view(student).balance());
        assertEquals(0, achievements.view(student).points());
        assertTrue(achievements.view(student).badges().stream().noneMatch(b -> b.earnedAt() != null));
        assertFalse(eligibility.isEligibleForExtraCourses(student));
        var after = metrics.find("gamificacao.moedas.creditadas").counter();
        assertEquals(coinsBefore, after == null ? 0 : after.count());
    }
}
