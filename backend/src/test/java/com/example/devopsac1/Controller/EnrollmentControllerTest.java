package com.example.devopsac1.Controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.devopsac1.curso.internal.Course;
import com.example.devopsac1.matricula.internal.Enrollment;
import com.example.devopsac1.aluno.internal.Student;
import com.example.devopsac1.curso.internal.CourseRepository;
import com.example.devopsac1.matricula.internal.EnrollmentRepository;
import com.example.devopsac1.aluno.internal.StudentRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private Long courseId;
    private Long studentId;

    @BeforeEach
    void setUp() {
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        studentRepository.deleteAll();

        courseId = courseRepository.save(new Course("Advanced Java")).getId();
        studentId = studentRepository.save(new Student("Kevin")).getId();
    }

    @Test
    void createsEnrollment() throws Exception {
        mockMvc.perform(post("/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":" + courseId + ",\"studentId\":" + studentId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseId", is(courseId.intValue())))
                .andExpect(jsonPath("$.studentId", is(studentId.intValue())))
                .andExpect(jsonPath("$.completed", is(false)));
    }

    @Test
    void creatingEnrollmentWithMissingCourseReturns404() throws Exception {
        mockMvc.perform(post("/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":999,\"studentId\":" + studentId + "}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void creatingEnrollmentWithMissingStudentReturns404() throws Exception {
        mockMvc.perform(post("/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":" + courseId + ",\"studentId\":999}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void completingMissingEnrollmentReturns404() throws Exception {
        mockMvc.perform(post("/enrollments/999/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grade\":8.5}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void completesEnrollmentWithValidGrade() throws Exception {
        long enrollmentId = enrollmentRepository.save(new Enrollment(courseId, studentId)).getId();

        mockMvc.perform(post("/enrollments/" + enrollmentId + "/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grade\":8.5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed", is(true)))
                .andExpect(jsonPath("$.grade", is(8.5)));
    }

    @Test
    void rejectsGradeAboveTen() throws Exception {
        long enrollmentId = enrollmentRepository.save(new Enrollment(courseId, studentId)).getId();

        mockMvc.perform(post("/enrollments/" + enrollmentId + "/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grade\":15}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void completeFlowPreservesApiAndEligibility() throws Exception {
        MvcResult created = mockMvc.perform(post("/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":" + courseId + ",\"studentId\":" + studentId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseName", is("Advanced Java")))
                .andExpect(jsonPath("$.studentName", is("Kevin")))
                .andReturn();
        Number enrollmentId = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(post("/enrollments/" + enrollmentId + "/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grade\":8.5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grade", is(8.5)));
        mockMvc.perform(get("/students/" + studentId + "/eligible-for-extra-courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligibleForExtraCourses", is(true)));
    }
}
