package com.example.demo.Model;

public class Student {
    private final String name;

    public Student(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Student{name='" + name + "'}";
        }
}
