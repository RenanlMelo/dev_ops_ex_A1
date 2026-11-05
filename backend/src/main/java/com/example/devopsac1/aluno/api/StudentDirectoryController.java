package com.example.devopsac1.aluno.api;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudentDirectoryController {
    private final StudentDirectory directory;
    public StudentDirectoryController(StudentDirectory directory) { this.directory = directory; }

    @GetMapping("/students")
    public List<StudentView> search(@RequestParam(defaultValue = "") String search,
                                    @RequestParam(defaultValue = "50") int limit) {
        return directory.search(search, limit);
    }
}
