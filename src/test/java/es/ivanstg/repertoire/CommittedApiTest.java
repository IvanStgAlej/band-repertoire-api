package es.ivanstg.repertoire;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Base class for HTTP-level tests that are not wrapped in a test transaction: every request commits, so
 * constraints, flush order and ON DELETE CASCADE behave as in production. The data is deleted after each test.
 */
public abstract class CommittedApiTest extends HttpTest {

    @Autowired
    protected JdbcTemplate jdbc;

    /** Every other table cascades from these two. */
    @AfterEach
    void deleteAllData() {
        jdbc.update("DELETE FROM ensemble");
        jdbc.update("DELETE FROM musician");
    }

    protected int count(String table) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    }
}
