package com.example.devopsac1.Test;

import com.example.devopsac1.Domain.Course;
import com.example.devopsac1.Domain.Enrollment;
import com.example.devopsac1.Domain.Student;
import com.example.devopsac1.Service.CourseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CourseServiceTest {

    private final CourseService courseService = new CourseService();

    // Dado um curso e Kevin, um Participante valido
    // Quando o curso for finalizado E a nota for acima de 7,0
    // Entao o usuario tem direito a realizacao de mais 3 cursos
    @DisplayName("Kevin: curso concluido com media acima de 7 permite cursos extras")
    @ParameterizedTest
    @ValueSource(doubles = {7.01, 8.5, 10.0})
    public void kevin_gradeAbove7_isEligibleForExtraCourses(double grade) {
        // Dado
        Enrollment enrollment = enrollmentFor("Kevin");

        // Quando
        courseService.completeCourse(enrollment, grade);

        // Entao
        assertTrue(enrollment.isCompleted());
        assertEquals(grade, courseService.getAverage(enrollment), 0.0001);
        assertTrue(courseService.isEligibleForExtraCourses(enrollment));
    }

    @DisplayName("Kevin: media igual a 7 nao satisfaz a regra acima de 7")
    @Test
    public void kevin_gradeExactly7_isNotEligibleForExtraCourses() {
        Enrollment enrollment = enrollmentFor("Kevin");

        courseService.completeCourse(enrollment, 7.0);

        assertEquals(7.0, courseService.getAverage(enrollment), 0.0001);
        assertFalse(courseService.isEligibleForExtraCourses(enrollment));
    }

    @DisplayName("Kevin: curso ainda nao concluido nao permite cursos extras")
    @Test
    public void kevin_courseNotCompleted_isNotEligibleForExtraCourses() {
        Enrollment enrollment = enrollmentFor("Kevin");

        assertFalse(courseService.isEligibleForExtraCourses(enrollment));
    }

    // Dado um curso e Renan, um Participante valido
    // Quando o curso for finalizado E a nota for abaixo de 7,0
    // Entao o usuario nao tera direito a realizacao de mais 3 cursos
    @Test
    public void renan_gradeBelow7_isNotEligibleForExtraCourses() {
        Enrollment enrollment = enrollmentFor("Renan");

        courseService.completeCourse(enrollment, 6.5);

        assertFalse(courseService.isEligibleForExtraCourses(enrollment));
    }

    // Dado um curso e Roberto, um Participante valido
    // Enquanto o curso nao for concluido
    // Entao o usuario nao tera acesso a sua media do curso
    @Test
    public void roberto_courseNotCompleted_hasNoAccessToAverage() {
        Enrollment enrollment = enrollmentFor("Roberto");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> courseService.getAverage(enrollment));

        assertTrue(exception.getMessage().contains("completed"));
    }

    // BDD implicito: quando o curso for finalizado, a matricula fica
    // concluida com a nota informada (pre-condicao usada por Kevin e Renan).
    @Test
    public void completingCourse_marksEnrollmentCompletedWithGrade() {
        Enrollment enrollment = enrollmentFor("Kevin");

        courseService.completeCourse(enrollment, 9.0);

        assertTrue(enrollment.isCompleted());
        assertEquals(9.0, enrollment.getGrade(), 0.0001);
    }

    // BDD complementar ao cenario do Roberto: uma vez o curso concluido,
    // o usuario passa a ter acesso a sua media.
    @Test
    public void completedCourse_hasAccessToAverage() {
        Enrollment enrollment = enrollmentFor("Roberto");

        courseService.completeCourse(enrollment, 7.5);

        assertEquals(7.5, courseService.getAverage(enrollment), 0.0001);
    }
    private Enrollment enrollmentFor(String studentName) {
        return new Enrollment(new Course("Advanced Java"), new Student(studentName));
    }
}
