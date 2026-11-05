package com.example.devopsac1.aluno.internal;

import com.example.devopsac1.aluno.api.StudentLookup;
import com.example.devopsac1.aluno.api.StudentCommand;
import com.example.devopsac1.aluno.api.StudentResponse;
import com.example.devopsac1.aluno.api.StudentView;
import com.example.devopsac1.aluno.api.StudentDirectory;
import com.example.devopsac1.aluno.api.StudentLock;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Propagation;
import com.example.devopsac1.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService implements StudentLookup, StudentCommand, StudentDirectory, StudentLock {
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

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void lock(Long studentId) {
        repository.lockById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student " + studentId + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentView> search(String search, int limit) {
        return repository.findByNameContainingIgnoreCaseOrderByNameAsc(search,
                PageRequest.of(0, Math.clamp(limit, 1, 100))).stream()
                .map(s -> new StudentView(s.getId(), s.getName())).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, String> names(Collection<Long> ids) {
        return repository.findAllById(ids).stream()
                .collect(Collectors.toMap(Student::getId, Student::getName));
    }
}
