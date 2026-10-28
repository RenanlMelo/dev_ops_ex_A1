package com.example.devopsac1.matricula.api;

import com.example.devopsac1.aluno.api.StudentLookup;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EligibilityController {
    private final StudentLookup students;
    private final EligibilityQuery eligibility;

    public EligibilityController(StudentLookup students, EligibilityQuery eligibility) {
        this.students = students;
        this.eligibility = eligibility;
    }

    @GetMapping("/students/{id}/eligible-for-extra-courses")
    public EligibilityResponse eligibility(@PathVariable Long id) {
        students.require(id);
        return new EligibilityResponse(id, eligibility.isEligibleForExtraCourses(id));
    }
}
