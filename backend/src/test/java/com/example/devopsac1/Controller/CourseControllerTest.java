package com.example.devopsac1.Controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.devopsac1.Repository.CourseRepository;
import com.example.devopsac1.Repository.EnrollmentRepository;
import com.example.devopsac1.Repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @BeforeEach
    void cleanUp() {
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        studentRepository.deleteAll();
    }

    @Test
    void createsAndReturnsCourse() throws Exception {
        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Advanced Java\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is("Advanced Java")));
    }

    @Test
    void rejectsBlankCourseName() throws Exception {
        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void getMissingCourseReturns404() throws Exception {
        mockMvc.perform(get("/courses/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void getsExistingCourse() throws Exception {
        long id = createCourse("Advanced Java");

        mockMvc.perform(get("/courses/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) id)))
                .andExpect(jsonPath("$.name", is("Advanced Java")));
    }

    @Test
    void deletingMissingCourseReturns404() throws Exception {
        mockMvc.perform(delete("/courses/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listsCourseRoster() throws Exception {
        long id = createCourse("Advanced Java");
        long studentId = studentRepository.save(new com.example.devopsac1.Domain.Student("Kevin")).getId();
        enrollmentRepository.save(new com.example.devopsac1.Domain.Enrollment(
                courseRepository.findById(id).orElseThrow(),
                studentRepository.findById(studentId).orElseThrow()
        ));

        mockMvc.perform(get("/courses/" + id + "/enrollments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].courseId", is((int) id)));
    }

    @Test
    void updatesCourseName() throws Exception {
        long id = createCourse("Old Name");

        mockMvc.perform(put("/courses/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New Name")));
    }

    @Test
    void deletesCourse() throws Exception {
        long id = createCourse("To Delete");

        mockMvc.perform(delete("/courses/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/courses/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void listsCoursesPaginated() throws Exception {
        createCourse("Course A");
        createCourse("Course B");

        mockMvc.perform(get("/courses?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content.length()", is(2)));
    }

    @Test
    void courseAverageIsZeroWithNoCompletedEnrollments() throws Exception {
        long id = createCourse("Empty Course");

        mockMvc.perform(get("/courses/" + id + "/average"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.average", is(0.0)));
    }

    @Test
    void courseAverageReflectsCompletedEnrollments() throws Exception {
        long id = createCourse("Advanced Java");
        long studentId = studentRepository.save(new com.example.devopsac1.Domain.Student("Kevin")).getId();
        com.example.devopsac1.Domain.Enrollment enrollment = new com.example.devopsac1.Domain.Enrollment(
                courseRepository.findById(id).orElseThrow(),
                studentRepository.findById(studentId).orElseThrow()
        );
        enrollment.complete(8.0);
        enrollmentRepository.save(enrollment);

        mockMvc.perform(get("/courses/" + id + "/average"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.average", is(8.0)));
    }

    private long createCourse(String name) {
        return courseRepository.save(new com.example.devopsac1.Domain.Course(name)).getId();
    }
}
