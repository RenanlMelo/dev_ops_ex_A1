package com.example.devopsac1.aluno.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface StudentDirectory {
    List<StudentView> search(String search, int limit);
    Map<Long, String> names(Collection<Long> ids);
}
