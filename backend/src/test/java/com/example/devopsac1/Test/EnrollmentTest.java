package com.example.devopsac1.Test;

import com.example.devopsac1.Domain.Course;
import com.example.devopsac1.Domain.Enrollment;
import com.example.devopsac1.Domain.Student;
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
        Course course = new Course("Advanced Java");
        Student student = new Student("Roberto");
        enrollment = new Enrollment(course, student);
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
    public void exposesCourseAndStudent() {
        assertEquals("Advanced Java", enrollment.getCourse().getName());
        assertEquals("Roberto", enrollment.getStudent().getName());
    }
}
