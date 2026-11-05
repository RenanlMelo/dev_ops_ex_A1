package com.example.devopsac1.matricula.internal;

import com.example.devopsac1.aluno.api.StudentCommand;
import com.example.devopsac1.aluno.api.StudentLookup;
import com.example.devopsac1.curso.api.CourseCatalog;
import com.example.devopsac1.curso.api.CourseCommand;
import com.example.devopsac1.curso.api.CourseView;
import com.example.devopsac1.matricula.api.EnrollmentCommand;
import com.example.devopsac1.matricula.api.LearningSpace;
import com.example.devopsac1.matricula.api.LearningSpaceQuery;
import com.example.devopsac1.shared.ResourceNotFoundException;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LearningSpaceService implements LearningSpaceQuery {
    private static final String PROFILE = "principal";
    private final LearningProfileRepository profiles;
    private final EnrollmentRepository enrollments;
    private final StudentCommand students;
    private final StudentLookup studentLookup;
    private final CourseCommand courses;
    private final CourseCatalog catalog;
    private final EnrollmentCommand enrollmentCommand;

    public LearningSpaceService(LearningProfileRepository profiles, EnrollmentRepository enrollments,
            StudentCommand students, StudentLookup studentLookup, CourseCommand courses,
            CourseCatalog catalog, EnrollmentCommand enrollmentCommand) {
        this.profiles = profiles; this.enrollments = enrollments; this.students = students;
        this.studentLookup = studentLookup; this.courses = courses; this.catalog = catalog;
        this.enrollmentCommand = enrollmentCommand;
    }

    /** Executado uma vez na inicialização; reabrir a página nunca redefine o progresso. */
    @Transactional
    public void initialize(String name) {
        if (profiles.existsById(PROFILE)) return;
        Long studentId = students.create(name).id();
        String[] titles = {"Fundamentos de Programação", "Java e Orientação a Objetos",
            "Banco de Dados Relacional", "Testes de Software", "Git e Integração Contínua", "Arquitetura de Software"};
        double[] grades = {8.5, 9, 8, 10, 9.5};
        for (int i = 0; i < titles.length; i++) {
            Long courseId = courses.create(titles[i]).id();
            var enrollment = enrollmentCommand.create(courseId, studentId);
            if (i < grades.length) enrollmentCommand.complete(enrollment.id(), grades[i]);
        }
        profiles.save(new LearningProfile(PROFILE, studentId));
    }

    @Override
    @Transactional(readOnly = true)
    public LearningSpace view() {
        var profile = profiles.findById(PROFILE)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de aprendizagem não inicializado"));
        var enrolled = enrollments.findByStudentId(profile.getStudentId());
        Map<Long, String> names = catalog.findAll(enrolled.stream().map(Enrollment::getCourseId).toList())
                .stream().collect(Collectors.toMap(CourseView::id, CourseView::name));
        var items = enrolled.stream().sorted(Comparator.comparing(Enrollment::isCompleted)
                .thenComparing(Enrollment::getId)).map(e -> new LearningSpace.Course(e.getCourseId(), e.getId(),
                    names.get(e.getCourseId()), e.isCompleted(), e.getGrade())).toList();
        return new LearningSpace(studentLookup.require(profile.getStudentId()), items);
    }
}
