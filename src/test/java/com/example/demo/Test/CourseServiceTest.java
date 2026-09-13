package com.example.demo.Test;

import com.example.demo.Model.Course;
import com.example.demo.Model.Enrollment;
import com.example.demo.Model.Student;
import com.example.demo.Service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CourseServiceTest {

    private CourseService courseService;
    private Enrollment enrollment;

    @BeforeEach
    public void setUp() {
        courseService = new CourseService();
        Course course = new Course("Advanced Java");
        Student student = new Student("Roberto");
        enrollment = new Enrollment(course, student);
    }

    @Test
    public void grantsExtraCoursesAboveAverage() {
        courseService.completeCourse(enrollment, 6.5);

        assertTrue(courseService.isEligibleForExtraCourses(enrollment));
    }

    @Test
    public void deniesExtraCoursesAtOrBelowAverage() {
        courseService.completeCourse(enrollment, 8.5);

        assertFalse(courseService.isEligibleForExtraCourses(enrollment));
    }

    @Test
    public void throwsWhenCourseNotCompleted() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> courseService.getAverage(enrollment));

        assertTrue(exception.getMessage().contains("completed"));
    }

    @Test
    public void returnsAverageAfterCompletion() {
        courseService.completeCourse(enrollment, 6.0);

        assertEquals(9.0, courseService.getAverage(enrollment), 0.0001);
    }
}