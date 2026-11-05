package com.example.devopsac1.curso.api;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CourseCatalogController {
    private final CourseCatalog catalog;
    public CourseCatalogController(CourseCatalog catalog) { this.catalog = catalog; }

    @GetMapping("/courses")
    public List<CourseView> list(@RequestParam(defaultValue = "50") int limit) {
        return catalog.list(limit);
    }
}
