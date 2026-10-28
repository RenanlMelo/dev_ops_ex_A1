package com.example.devopsac1.matricula.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnrollmentRulesTest {

    // Dado um curso e Kevin, um Participante valido
    // Quando o curso for finalizado E a nota for acima de 7,0
    // Entao o usuario tem direito a realizacao de mais 3 cursos
    @DisplayName("Kevin: curso concluido com media acima de 7 permite cursos extras")
    @ParameterizedTest
    @ValueSource(doubles = {7.01, 8.5, 10.0})
    public void kevin_gradeAbove7_isEligibleForExtraCourses(double grade) {
        // Dado
        Enrollment enrollment = enrollmentFor();

        // Quando
        enrollment.complete(grade);

        // Entao
        assertCompletedWithAverage(enrollment, grade);
        assertTrue(enrollment.hasGradeAboveMinimum());
    }

    @DisplayName("Kevin: media igual a 7 nao satisfaz a regra acima de 7")
    @Test
    public void kevin_gradeExactly7_isNotEligibleForExtraCourses() {
        Enrollment enrollment = enrollmentFor();

        enrollment.complete(7.0);

        assertEquals(7.0, enrollment.getAverage(), 0.0001);
        assertFalse(enrollment.hasGradeAboveMinimum());
    }

    @DisplayName("Kevin: curso ainda nao concluido nao permite cursos extras")
    @Test
    public void kevin_courseNotCompleted_isNotEligibleForExtraCourses() {
        Enrollment enrollment = enrollmentFor();

        assertFalse(enrollment.hasGradeAboveMinimum());
    }

    // Dado um curso e Renan, um Participante valido
    // Quando o curso for finalizado E a nota for abaixo de 7,0
    // Entao o usuario nao tera direito a realizacao de mais 3 cursos
    @DisplayName("Renan: media abaixo de 7 reprova e nao permite cursos extras")
    @ParameterizedTest
    @ValueSource(doubles = {0.0, 5.0, 6.5, 6.99})
    public void renan_gradeBelow7_isNotEligibleForExtraCourses(double grade) {
        // Dado um curso e um participante valido
        Enrollment enrollment = enrollmentFor();

        // Quando o curso for concluido com media abaixo de 7
        enrollment.complete(grade);

        // Entao a media fica disponivel, mas nao ha direito a cursos extras
        assertCompletedWithAverage(enrollment, grade);
        assertFalse(enrollment.hasGradeAboveMinimum());
    }

    // Dado um curso e Roberto, um Participante valido
    // Enquanto o curso nao for concluido
    // Entao o usuario nao tera acesso a sua media do curso
    @DisplayName("Roberto: curso nao concluido impede o acesso a media")
    @Test
    public void roberto_courseNotCompleted_hasNoAccessToAverage() {
        // Dado um curso e um participante valido, sem conclusao do curso
        Enrollment enrollment = enrollmentFor();
        assertFalse(enrollment.isCompleted());
        assertNull(enrollment.getGrade());

        // Quando tentar consultar a media, entao o acesso deve ser recusado
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> enrollment.getAverage());

        assertEquals("Average unavailable: the course has not been completed yet.",
                exception.getMessage());
        assertFalse(enrollment.isCompleted());
        assertNull(enrollment.getGrade());
    }

    // BDD implicito: quando o curso for finalizado, a matricula fica
    // concluida com a nota informada (pre-condicao usada por Kevin e Renan).
    @Test
    public void completingCourse_marksEnrollmentCompletedWithGrade() {
        Enrollment enrollment = enrollmentFor();

        enrollment.complete(9.0);

        assertTrue(enrollment.isCompleted());
        assertEquals(9.0, enrollment.getGrade(), 0.0001);
    }

    // BDD complementar ao cenario do Roberto: uma vez o curso concluido,
    // o usuario passa a ter acesso a sua media.
    @DisplayName("Roberto: apos concluir o curso, a media fica disponivel para qualquer nota valida")
    @ParameterizedTest
    @ValueSource(doubles = {0.0, 6.99, 7.0, 8.5, 10.0})
    public void completedCourse_hasAccessToAverage(double grade) {
        Enrollment enrollment = enrollmentFor();

        enrollment.complete(grade);

        assertCompletedWithAverage(enrollment, grade);
    }

    private void assertCompletedWithAverage(Enrollment enrollment, double expectedGrade) {
        assertTrue(enrollment.isCompleted());
        assertEquals(expectedGrade, enrollment.getAverage(), 0.0001);
    }

    private Enrollment enrollmentFor() {
        return new Enrollment(1L, 2L);
    }
}
