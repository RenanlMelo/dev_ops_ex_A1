package com.example.devopsac1.Test;

import com.example.devopsac1.Domain.Course;
import com.example.devopsac1.Domain.Enrollment;
import com.example.devopsac1.Domain.Student;
import com.example.devopsac1.Service.CourseService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CourseServiceTest {

    private final CourseService courseService = new CourseService();

    // Dado um curso e Kevin, um Participante valido
    // Quando o curso for finalizado E a nota for acima de 7,0
    // Entao o usuario tem direito a realizacao de mais 3 cursos
    @Test
    public void kevin_gradeAbove7_isEligibleForExtraCourses() {
        Course course = new Course("Advanced Java");
        Student kevin = new Student("Kevin");
        Enrollment enrollment = new Enrollment(course, kevin);

        courseService.completeCourse(enrollment, 8.5);

        assertTrue(courseService.isEligibleForExtraCourses(enrollment));
    }

    // Dado um curso e Renan, um Participante valido
    // Quando o curso for finalizado E a nota for abaixo de 7,0
    // Entao o usuario nao tera direito a realizacao de mais 3 cursos
    @Test
    public void renan_gradeBelow7_isNotEligibleForExtraCourses() {
        Course course = new Course("Advanced Java");
        Student renan = new Student("Renan");
        Enrollment enrollment = new Enrollment(course, renan);

        courseService.completeCourse(enrollment, 6.5);

        assertFalse(courseService.isEligibleForExtraCourses(enrollment));
    }

    // Dado um curso e Roberto, um Participante valido
    // Enquanto o curso nao for concluido
    // Entao o usuario nao tera acesso a sua media do curso
    @Test
    public void roberto_courseNotCompleted_hasNoAccessToAverage() {
        Course course = new Course("Advanced Java");
        Student roberto = new Student("Roberto");
        Enrollment enrollment = new Enrollment(course, roberto);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> courseService.getAverage(enrollment));

        assertTrue(exception.getMessage().contains("completed"));
    }

    // BDD implicito: quando o curso for finalizado, a matricula fica
    // concluida com a nota informada (pre-condicao usada por Kevin e Renan).
    @Test
    public void completingCourse_marksEnrollmentCompletedWithGrade() {
        Course course = new Course("Advanced Java");
        Student student = new Student("Kevin");
        Enrollment enrollment = new Enrollment(course, student);

        courseService.completeCourse(enrollment, 9.0);

        assertTrue(enrollment.isCompleted());
        assertEquals(9.0, enrollment.getGrade(), 0.0001);
    }

    // BDD complementar ao cenario do Roberto: uma vez o curso concluido,
    // o usuario passa a ter acesso a sua media.
    @Test
    public void completedCourse_hasAccessToAverage() {
        Course course = new Course("Advanced Java");
        Student student = new Student("Roberto");
        Enrollment enrollment = new Enrollment(course, student);

        courseService.completeCourse(enrollment, 7.5);

        assertEquals(7.5, courseService.getAverage(enrollment), 0.0001);
    }
}
