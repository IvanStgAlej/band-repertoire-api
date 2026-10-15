package es.ivanstg.repertoire;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Guards the list endpoints against N+1 queries: the query count must not grow with the page size. */
class QueryCountTest extends ApiTest {

    private static final int ROWS = 10;
    private static final long MAX_QUERIES = 12;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private long band;

    @BeforeEach
    void createBandWithHistory() throws Exception {
        band = createEnsemble("Riverside Concert Band");
        long[] musicians = new long[4];
        for (int i = 0; i < musicians.length; i++) {
            musicians[i] = createMusician("Musician " + i, "musician" + i + "@example.com");
            addMember(band, musicians[i]);
        }
        for (int i = 0; i < ROWS; i++) {
            long first = createPiece(band, "Piece A" + i, 200, 2);
            long second = createPiece(band, "Piece B" + i, 300, 3);
            long rehearsal = createRehearsal(band, "2026-09-%02dT19:30:00".formatted(i + 1), first, second);
            putJson("/api/rehearsals/" + rehearsal + "/attendance", """
                    {"entries": [{"musicianId": %d, "attended": true}, {"musicianId": %d, "attended": false}]}"""
                    .formatted(musicians[0], musicians[1]))
                    .andExpect(status().isOk());
            long concert = createConcert(band, "Concert " + i, "2026-12-%02d".formatted(i + 1));
            setProgram(concert, first, second).andExpect(status().isOk());
        }
        flushAndClear();
    }

    @Test
    void listingRehearsalsDoesNotQueryPerRow() throws Exception {
        long queries = countQueries(() -> mvc.perform(get("/api/ensembles/" + band + "/rehearsals?size=" + ROWS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(ROWS)));

        assertThat(queries).isLessThanOrEqualTo(MAX_QUERIES);
    }

    @Test
    void listingConcertsDoesNotQueryPerRow() throws Exception {
        long queries = countQueries(() -> mvc.perform(get("/api/ensembles/" + band + "/concerts?size=" + ROWS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(ROWS)));

        assertThat(queries).isLessThanOrEqualTo(MAX_QUERIES);
    }

    private long countQueries(ThrowingRunnable request) throws Exception {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        request.run();
        return statistics.getPrepareStatementCount();
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
