package com.example.devopsac1.matricula.internal;

import com.example.devopsac1.aluno.api.StudentLookup;
import com.example.devopsac1.aluno.api.StudentView;
import com.example.devopsac1.curso.api.CourseLookup;
import com.example.devopsac1.curso.api.CourseView;
import com.example.devopsac1.matricula.api.EligibilityQuery;
import com.example.devopsac1.matricula.api.EnrollmentCommand;
import com.example.devopsac1.matricula.api.EnrollmentResponse;
import com.example.devopsac1.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentService implements EligibilityQuery, EnrollmentCommand {
    private final EnrollmentRepository repository;
    private final CourseLookup courses;
    private final StudentLookup students;
    private final com.example.devopsac1.aluno.api.StudentLock studentLock;
    private final EnrollmentRankingUpdater ranking;

    public EnrollmentService(EnrollmentRepository repository, CourseLookup courses, StudentLookup students,
                             com.example.devopsac1.aluno.api.StudentLock studentLock, EnrollmentRankingUpdater ranking) {
        this.repository = repository;
        this.courses = courses;
        this.students = students;
        this.studentLock = studentLock;
        this.ranking = ranking;
    }

    @Transactional
    public EnrollmentResponse create(Long courseId, Long studentId) {
        CourseView course = courses.require(courseId);
        StudentView student = students.require(studentId);
        Enrollment enrollment = repository.save(new Enrollment(course.id(), student.id()));
        return response(enrollment, course, student);
    }

    @Transactional
    public EnrollmentResponse complete(Long id, double grade) {
        Enrollment enrollment = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment " + id + " not found"));
        studentLock.lock(enrollment.getStudentId());
        enrollment.complete(grade);
        CourseView course = courses.require(enrollment.getCourseId());
        StudentView student = students.require(enrollment.getStudentId());
        Enrollment saved = repository.saveAndFlush(enrollment);
        ranking.refresh(student.id());
        return response(saved, course, student);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEligibleForExtraCourses(Long studentId) {
        return repository.findByStudentId(studentId).stream().anyMatch(Enrollment::hasGradeAboveMinimum);
    }

    private EnrollmentResponse response(Enrollment enrollment, CourseView course, StudentView student) {
        return new EnrollmentResponse(enrollment.getId(), course.id(), course.name(),
                student.id(), student.name(), enrollment.isCompleted(), enrollment.getGrade());
    }
}
