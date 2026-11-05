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
class GamificationApiTest {
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
    void rewardRedemptionDebitsOnceAndPersistsEntitlement() throws Exception {
        complete(enrollment(student), 8.5);
        String route = "/students/" + student + "/redemptions";
        String body = "{\"rewardCode\":\"material-extra\",\"requestId\":\"same-request\"}";
        for (int attempt = 0; attempt < 2; attempt++) {
            mvc.perform(post(route).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.cost", is(50)));
        }
        coins(0);
        mvc.perform(get(route)).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(post(route).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rewardCode\":\"curso-bonus\",\"requestId\":\"same-request\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void insufficientBalanceDoesNotCreateRedemptionOrDebit() throws Exception {
        String route = "/students/" + student + "/redemptions";
        mvc.perform(post(route).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rewardCode\":\"mentoria\",\"requestId\":\"no-coins\"}"))
                .andExpect(status().isConflict());
        mvc.perform(get(route)).andExpect(jsonPath("$", hasSize(0)));
        coins(0);
    }

    @Test
    void validatesUnknownStudentsRewardAndRequestId() throws Exception {
        mvc.perform(get("/students/999999/wallet")).andExpect(status().isNotFound());
        mvc.perform(get("/ranking/students/999999")).andExpect(status().isNotFound());
        mvc.perform(post("/students/" + student + "/redemptions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rewardCode\":\"missing\",\"requestId\":\"valid\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/students/" + student + "/redemptions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rewardCode\":\"material-extra\",\"requestId\":\"bad key!\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/rewards")).andExpect(jsonPath("$", hasSize(3)));
        mvc.perform(get("/students?search=Aluno B1")).andExpect(jsonPath("$[0].name", is("Aluno B1")));
    }
}
