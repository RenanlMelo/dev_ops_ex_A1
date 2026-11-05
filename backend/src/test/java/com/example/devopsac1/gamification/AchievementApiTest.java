package com.example.devopsac1.gamification;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jayway.jsonpath.JsonPath;
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
class AchievementApiTest {
    @Autowired MockMvc mvc;
    private long student;

    @BeforeEach
    void createStudent() throws Exception { student = create("/students", "{\"name\":\"Aluno B1\"}"); }

    private long create(String route, String json) throws Exception {
        var result = mvc.perform(post(route).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated()).andReturn();
        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }
    private long enrollment(long learner) throws Exception {
        long course = create("/courses", "{\"name\":\"Curso B1\"}");
        return create("/enrollments", "{\"studentId\":" + learner + ",\"courseId\":" + course + "}");
    }
    private void complete(long enrollment, double grade) throws Exception {
        mvc.perform(post("/enrollments/" + enrollment + "/complete").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grade\":" + grade + "}"))
                .andExpect(status().isOk());
    }
    private void coins(long amount) throws Exception {
        mvc.perform(get("/students/" + student + "/wallet")).andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", is((int) amount)));
    }

    @Test
    void completionPersistsRankingCoinsAndBadge() throws Exception {
        mvc.perform(get("/ranking/students/" + student)).andExpect(jsonPath("$.position", nullValue()));
        coins(0);
        complete(enrollment(student), 8.5);
        coins(50);
        mvc.perform(get("/ranking/students/" + student)).andExpect(jsonPath("$.points", is(85)))
                .andExpect(jsonPath("$.completedCourses", is(1)));
        mvc.perform(get("/students/" + student + "/achievements"))
                .andExpect(jsonPath("$.badges[?(@.code == 'primeira-aprovacao')].earnedAt", hasSize(1)))
                .andExpect(jsonPath("$.approvedCourses", is(1)));
    }

    @Test
    void gradeSevenDoesNotGiveCoinsOrApprovalBadge() throws Exception {
        complete(enrollment(student), 7);
        coins(0);
        mvc.perform(get("/students/" + student + "/achievements"))
                .andExpect(jsonPath("$.points", is(70)))
                .andExpect(jsonPath("$.approvedCourses", is(0)))
                .andExpect(jsonPath("$.badges[0].earnedAt", nullValue()));
    }

    @Test
    void retryAndGradeRevisionDoNotDuplicateAwards() throws Exception {
        long id = enrollment(student);
        complete(id, 8.5);
        complete(id, 8.5);
        complete(id, 10);
        coins(50);
        mvc.perform(get("/ranking/students/" + student)).andExpect(jsonPath("$.points", is(100)))
                .andExpect(jsonPath("$.completedCourses", is(1)));
        complete(id, 4.5);
        coins(50);
        mvc.perform(get("/ranking/students/" + student)).andExpect(jsonPath("$.points", is(45)))
                .andExpect(jsonPath("$.approvedCourses", is(0)));
        mvc.perform(get("/students/" + student + "/achievements"))
                .andExpect(jsonPath("$.badges[?(@.earnedAt != null)]", hasSize(2)));
    }

    @Test
    void threeDistinctCoursesUnlockAllBadges() throws Exception {
        complete(enrollment(student), 10);
        complete(enrollment(student), 8.5);
        complete(enrollment(student), 9);
        coins(150);
        mvc.perform(get("/students/" + student + "/achievements"))
                .andExpect(jsonPath("$.badges[?(@.earnedAt != null)]", hasSize(3)))
                .andExpect(jsonPath("$.points", is(275)));
    }

    @Test
    void duplicateEnrollmentForSameCourseUsesSameRecord() throws Exception {
        long course = create("/courses", "{\"name\":\"Curso único\"}");
        String body = "{\"studentId\":" + student + ",\"courseId\":" + course + "}";
        long first = create("/enrollments", body);
        assertEquals(first, create("/enrollments", body));
        complete(first, 8.5);
        complete(create("/enrollments", body), 8.5);
        coins(50);
    }

    @Test
    void rankingIsOrderedAndPagedWithStableTies() throws Exception {
        long second = create("/students", "{\"name\":\"Segundo\"}");
        complete(enrollment(student), 8.5);
        complete(enrollment(second), 10);
        mvc.perform(get("/ranking?size=1&page=0"))
                .andExpect(jsonPath("$.entries", hasSize(1)))
                .andExpect(jsonPath("$.entries[0].studentId", is((int) second)))
                .andExpect(jsonPath("$.entries[0].position", is(1)))
                .andExpect(jsonPath("$.totalElements", is(2)));
        complete(enrollment(student), 1.5);
        mvc.perform(get("/ranking?size=1&page=0"))
                .andExpect(jsonPath("$.entries[0].studentId", is((int) student)));
        mvc.perform(get("/ranking?page=-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/ranking?size=999")).andExpect(jsonPath("$.size", is(100)));
    }

}
