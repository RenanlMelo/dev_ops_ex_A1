package com.example.devopsac1.curso.api;

import java.util.List;
import java.util.Collection;

public interface CourseCatalog {
    List<CourseView> list(int limit);
    List<CourseView> findAll(Collection<Long> ids);
}
