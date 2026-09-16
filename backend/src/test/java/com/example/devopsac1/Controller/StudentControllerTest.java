package com.example.devopsac1.Controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.devopsac1.Domain.Course;
import com.example.devopsac1.Domain.Enrollment;
import com.example.devopsac1.Domain.Student;
import com.example.devopsac1.Repository.CourseRepository;
import com.example.devopsac1.Repository.EnrollmentRepository;
import com.example.devopsac1.Repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @BeforeEach
    void cleanUp() {
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        studentRepository.deleteAll();
    }

    @Test
    void createsAndReturnsStudent() throws Exception {
        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Kevin\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is("Kevin")));
    }

    @Test
    void rejectsBlankStudentName() throws Exception {
        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMissingStudentReturns404() throws Exception {
        mockMvc.perform(get("/students/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getsExistingStudent() throws Exception {
        long id = studentRepository.save(new Student("Kevin")).getId();

        mockMvc.perform(get("/students/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Kevin")));
    }

    @Test
    void deletingMissingStudentReturns404() throws Exception {
        mockMvc.perform(delete("/students/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listsStudentsPaginated() throws Exception {
        studentRepository.save(new Student("Kevin"));
        studentRepository.save(new Student("Renan"));

        mockMvc.perform(get("/students?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    void updatesStudentName() throws Exception {
        long id = studentRepository.save(new Student("Old Name")).getId();

        mockMvc.perform(put("/students/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New Name")));
    }

    @Test
    void deletesStudent() throws Exception {
        long id = studentRepository.save(new Student("To Delete")).getId();

        mockMvc.perform(delete("/students/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/students/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void eligibilityIsFalseWithoutCompletedEnrollments() throws Exception {
        long studentId = studentRepository.save(new Student("Renan")).getId();

        mockMvc.perform(get("/students/" + studentId + "/eligible-for-extra-courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligibleForExtraCourses", is(false)));
    }

    @Test
    void eligibilityIsTrueWithGradeAboveMinimum() throws Exception {
        Student student = studentRepository.save(new Student("Kevin"));
        Course course = courseRepository.save(new Course("Advanced Java"));
        Enrollment enrollment = new Enrollment(course, student);
        enrollment.complete(8.5);
        enrollmentRepository.save(enrollment);

        mockMvc.perform(get("/students/" + student.getId() + "/eligible-for-extra-courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligibleForExtraCourses", is(true)));
    }

    @Test
    void listsStudentEnrollments() throws Exception {
        Student student = studentRepository.save(new Student("Kevin"));
        Course course = courseRepository.save(new Course("Advanced Java"));
        enrollmentRepository.save(new Enrollment(course, student));

        mockMvc.perform(get("/students/" + student.getId() + "/enrollments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].studentId", is(student.getId().intValue())));
    }
}
