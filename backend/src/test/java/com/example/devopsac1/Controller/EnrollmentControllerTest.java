package com.example.devopsac1.Controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    void getsExistingEnrollment() throws Exception {
        long enrollmentId = enrollmentRepository.save(new Enrollment(
                courseRepository.findById(courseId).orElseThrow(),
                studentRepository.findById(studentId).orElseThrow()
        )).getId();

        mockMvc.perform(get("/enrollments/" + enrollmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId", is(courseId.intValue())))
                .andExpect(jsonPath("$.studentId", is(studentId.intValue())));
    }

    @Test
    void getMissingEnrollmentReturns404() throws Exception {
        mockMvc.perform(get("/enrollments/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void completesEnrollmentWithValidGrade() throws Exception {
        long enrollmentId = enrollmentRepository.save(new Enrollment(
                courseRepository.findById(courseId).orElseThrow(),
                studentRepository.findById(studentId).orElseThrow()
        )).getId();

        mockMvc.perform(post("/enrollments/" + enrollmentId + "/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grade\":8.5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed", is(true)))
                .andExpect(jsonPath("$.grade", is(8.5)));
    }

    @Test
    void rejectsGradeAboveTen() throws Exception {
        long enrollmentId = enrollmentRepository.save(new Enrollment(
                courseRepository.findById(courseId).orElseThrow(),
                studentRepository.findById(studentId).orElseThrow()
        )).getId();

        mockMvc.perform(post("/enrollments/" + enrollmentId + "/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grade\":15}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void averageBeforeCompletionReturns409() throws Exception {
        long enrollmentId = enrollmentRepository.save(new Enrollment(
                courseRepository.findById(courseId).orElseThrow(),
                studentRepository.findById(studentId).orElseThrow()
        )).getId();

        mockMvc.perform(get("/enrollments/" + enrollmentId + "/average"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    void averageAfterCompletionReturnsGrade() throws Exception {
        Enrollment enrollment = new Enrollment(
                courseRepository.findById(courseId).orElseThrow(),
                studentRepository.findById(studentId).orElseThrow()
        );
        enrollment.complete(7.5);
        long enrollmentId = enrollmentRepository.save(enrollment).getId();

        mockMvc.perform(get("/enrollments/" + enrollmentId + "/average"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.average", is(7.5)));
    }
}
