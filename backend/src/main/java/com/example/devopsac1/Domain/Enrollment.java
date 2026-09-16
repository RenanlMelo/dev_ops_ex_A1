package com.example.devopsac1.Domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Enrollment {

    private static final double MINIMUM_GRADE_FOR_EXTRA_COURSES = 7.0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    private Student student;

    private boolean completed;

    private Double grade;

    protected Enrollment() {
    }

    public Enrollment(Course course, Student student) {
        this.course = course;
        this.student = student;
        this.completed = false;
        this.grade = null;
    }

    public void complete(double grade) {
        this.completed = true;
        this.grade = grade;
    }

    public Long getId() {
        return id;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Double getGrade() {
        return grade;
    }

    public Course getCourse() {
        return course;
    }

    public Student getStudent() {
        return student;
    }

    //BLUE
    // grade nunca e null quando completed e true: complete(grade) sempre
    // define os dois juntos, entao a checagem extra de null seria um branch
    // morto (impossivel de exercitar) e o JaCoCo acusaria cobertura parcial.
    public boolean hasGradeAboveMinimum() {
        return completed && grade > MINIMUM_GRADE_FOR_EXTRA_COURSES;
    }
}
