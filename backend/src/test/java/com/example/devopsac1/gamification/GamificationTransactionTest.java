package com.example.devopsac1.gamification;

import static org.junit.jupiter.api.Assertions.*;

import com.example.devopsac1.aluno.api.StudentCommand;
import com.example.devopsac1.carteira.api.WalletQuery;
import com.example.devopsac1.curso.api.CourseCommand;
import com.example.devopsac1.matricula.api.EnrollmentCommand;
import com.example.devopsac1.recompensa.api.RewardCommand;
import com.example.devopsac1.recompensa.api.RewardQuery;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:gamification-concurrency;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=5000")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class GamificationTransactionTest {
    @Autowired StudentCommand students;
    @Autowired CourseCommand courses;
    @Autowired EnrollmentCommand enrollments;
    @Autowired WalletQuery wallet;
    @Autowired RewardCommand rewards;
    @Autowired RewardQuery redemptions;

    @Test
    void concurrentRedemptionsCannotOverdrawBalance() throws Exception {
        long student = students.create("Concurrent redemption").id();
        long course = courses.create("Wallet course").id();
        enrollments.complete(enrollments.create(course, student).id(), 8.5);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var start = new CountDownLatch(1);
            var first = pool.submit(() -> redeemWhenStarted(start, student, "attempt-1"));
            var second = pool.submit(() -> redeemWhenStarted(start, student, "attempt-2"));
            start.countDown();
            int accepted = first.get(10, TimeUnit.SECONDS) + second.get(10, TimeUnit.SECONDS);
            assertEquals(1, accepted);
        }
        assertEquals(0, wallet.view(student).balance());
        assertEquals(1, redemptions.redemptions(student).size());
    }

    private int redeemWhenStarted(CountDownLatch start, long student, String request) throws InterruptedException {
        start.await();
        try { rewards.redeem(student, "material-extra", request); return 1; }
        catch (IllegalStateException insufficient) { return 0; }
    }

}
