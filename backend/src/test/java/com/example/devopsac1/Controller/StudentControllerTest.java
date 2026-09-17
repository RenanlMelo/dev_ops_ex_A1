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
    void eligibilityForMissingStudentReturns404() throws Exception {
        mockMvc.perform(get("/students/999/eligible-for-extra-courses"))
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
}
