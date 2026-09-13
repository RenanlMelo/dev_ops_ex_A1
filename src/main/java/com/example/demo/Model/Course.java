package com.example.demo.Model;

public class Course {
    private final String name;

    public Course(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Course{name='" + name + "'}";
    }
}
