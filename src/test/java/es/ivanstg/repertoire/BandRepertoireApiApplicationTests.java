package es.ivanstg.repertoire;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Starts the full context: Flyway migrates the schema and Hibernate validates every entity against it. */
@SpringBootTest
@ActiveProfiles("test")
class BandRepertoireApiApplicationTests {

    @Test
    void contextLoads() {
    }
}
