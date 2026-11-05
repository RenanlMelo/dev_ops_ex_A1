package com.example.devopsac1.performance;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;

import jakarta.persistence.EntityManagerFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:ranking-capacity;DB_CLOSE_DELAY=-1",
        "spring.jpa.properties.hibernate.generate_statistics=true",
        "logging.level.org.hibernate.stat=OFF",
        "logging.level.org.hibernate.engine.internal.StatisticalLoggingSessionEventListener=OFF"
})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RankingCapacityTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc mvc;
    @Autowired EntityManagerFactory factory;

    @Test
    void pagedRankingHandlesOneHundredThousandRegisteredLearners() throws Exception {
        // Synthetic data stays in this isolated in-memory database, never in HOMOL.
        jdbc.update("insert into student(id, name) select 1000000+x, 'Aluno de carga ' || x from system_range(1,100000)");
        jdbc.update("insert into ranking_entry(student_id,points,completed_courses,approved_courses) "
                + "select 1000000+x, mod(x,1000), 10, 8 from system_range(1,100000)");
        var statistics = factory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        mvc.perform(get("/ranking?size=20")).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(100000)))
                .andExpect(jsonPath("$.entries.length()", is(20)));
        long statements = statistics.getPrepareStatementCount();
        assertTrue(statements <= 4, "Page queries must stay bounded; observed " + statements);
        for (int i = 0; i < 10; i++) mvc.perform(get("/ranking?size=20")).andExpect(status().isOk());

        var samples = Collections.synchronizedList(new ArrayList<Double>());
        long started = System.nanoTime();
        try (var pool = Executors.newFixedThreadPool(8)) {
            var start = new CountDownLatch(1);
            var tasks = new ArrayList<java.util.concurrent.Future<?>>();
            for (int worker = 0; worker < 8; worker++) {
                tasks.add(pool.submit(() -> {
                    start.await();
                    for (int request = 0; request < 20; request++) {
                        long before = System.nanoTime();
                        mvc.perform(get("/ranking?page=" + request + "&size=20")).andExpect(status().isOk());
                        samples.add((System.nanoTime() - before) / 1_000_000.0);
                    }
                    return null;
                }));
            }
            start.countDown();
            for (var task : tasks) task.get(60, TimeUnit.SECONDS);
        }
        double seconds = (System.nanoTime() - started) / 1_000_000_000.0;
        Collections.sort(samples);
        double p95 = samples.get((int) Math.ceil(samples.size() * .95) - 1);
        String report = String.format(Locale.ROOT,
                "{\"dataset\":100000,\"requests\":%d,\"concurrency\":8,\"p95_ms\":%.3f,"
                + "\"requests_per_second\":%.3f,\"sql_per_page\":%d,"
                + "\"environment\":\"MockMvc + JPA + isolated H2, no external HTTP\"}%n",
                samples.size(), p95, samples.size()/seconds, statements);
        Files.writeString(Path.of("target/ranking-benchmark.json"), report);
        assertEquals(160, samples.size());
        assertTrue(p95 < 1000, "Local p95 target is <1000 ms; observed " + p95);
    }
}
