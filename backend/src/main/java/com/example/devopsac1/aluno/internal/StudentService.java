package com.example.devopsac1.aluno.internal;

import com.example.devopsac1.aluno.api.StudentLookup;
import com.example.devopsac1.aluno.api.StudentCommand;
import com.example.devopsac1.aluno.api.StudentResponse;
import com.example.devopsac1.aluno.api.StudentView;
import com.example.devopsac1.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService implements StudentLookup, StudentCommand {
    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public StudentResponse create(String name) {
        Student student = repository.save(new Student(name));
        return new StudentResponse(student.getId(), student.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public StudentView require(Long id) {
        Student student = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student " + id + " not found"));
        return new StudentView(student.getId(), student.getName());
    }
}
