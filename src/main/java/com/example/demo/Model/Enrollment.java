package com.example.demo.Model;

public class Enrollment {

    private static final double MINIMUM_GRADE_FOR_EXTRA_COURSES = 7.0;

    private final Course course;
    private final Student student;
    private boolean completed;
    private Double grade;

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

    public boolean hasGradeAboveMinimum() {
        return completed && grade != null && grade > MINIMUM_GRADE_FOR_EXTRA_COURSES;
    }
}