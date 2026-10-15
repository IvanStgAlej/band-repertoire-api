package es.ivanstg.repertoire;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for HTTP-level tests that are rolled back: every test runs in one transaction that is never committed.
 */
@Transactional
public abstract class ApiTest extends HttpTest {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Every test runs in one transaction, so entities stay cached between requests. Call this to see what a
     * fresh request would see, e.g. after the database itself removed rows with ON DELETE CASCADE.
     */
    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
