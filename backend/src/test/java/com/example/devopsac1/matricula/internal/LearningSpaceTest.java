package com.example.devopsac1.matricula.internal;

import static org.junit.jupiter.api.Assertions.*;
import com.example.devopsac1.matricula.api.EnrollmentCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class LearningSpaceTest {
    @Autowired LearningSpaceService space;
    @Autowired EnrollmentCommand enrollments;

    @Test
    void initializesOnceWithFiveCompletedAndOnePendingWithoutDuplicatingAwards() {
        space.initialize("Renan");
        var first = space.view();
        space.initialize("Outro nome");
        var again = space.view();
        assertEquals(first, again);
        assertEquals(6, again.courses().size());
        assertEquals(1, again.courses().stream().filter(c -> !c.completed()).count());
    }

    @Test
    void reopeningSpaceKeepsCourseConclusionAndDoesNotResetTheProfile() {
        space.initialize("Renan");
        var pending = space.view().courses().stream().filter(c -> !c.completed()).findFirst().orElseThrow();
        enrollments.complete(pending.enrollmentId(), 8.5);
        space.initialize("Renan");
        var reopened = space.view();
        assertTrue(reopened.courses().stream().allMatch(c -> c.completed()));
    }
}
