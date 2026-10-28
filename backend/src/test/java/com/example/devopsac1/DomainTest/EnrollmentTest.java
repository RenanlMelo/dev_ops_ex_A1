package com.example.devopsac1.DomainTest;

import com.example.devopsac1.matricula.internal.Enrollment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnrollmentTest {

    private Enrollment enrollment;

    @BeforeEach
    public void setUp() {
        enrollment = new Enrollment(1L, 2L);
    }

    @Test
    public void startsNotCompletedWithoutGrade() {
        assertFalse(enrollment.isCompleted());
        assertNull(enrollment.getGrade());
    }

    @Test
    public void completeSetsCompletedAndGrade() {
        enrollment.complete(8.5);

        assertTrue(enrollment.isCompleted());
        assertEquals(8.5, enrollment.getGrade(), 0.0001);
    }

    @Test
    public void hasGradeAboveMinimumWhenGradeIsAboveThreshold() {
        enrollment.complete(8.5);

        assertTrue(enrollment.hasGradeAboveMinimum());
    }

    @Test
    public void hasGradeAboveMinimumIsFalseAtThreshold() {
        enrollment.complete(7.0);

        assertFalse(enrollment.hasGradeAboveMinimum());
    }

    @Test
    public void hasGradeAboveMinimumIsFalseBelowThreshold() {
        enrollment.complete(6.5);

        assertFalse(enrollment.hasGradeAboveMinimum());
    }

    @Test
    public void hasGradeAboveMinimumIsFalseWhenNotCompleted() {
        assertFalse(enrollment.hasGradeAboveMinimum());
    }

    @Test
    public void exposesCourseAndStudentIds() {
        assertEquals(1L, enrollment.getCourseId());
        assertEquals(2L, enrollment.getStudentId());
    }
}
