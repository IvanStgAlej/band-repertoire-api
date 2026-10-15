package es.ivanstg.repertoire.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;

class GlobalExceptionHandlerTest {

    @Test
    void unexpectedErrorsBecomeA500ProblemDetailWithoutInternals() {
        ProblemDetail problem = new GlobalExceptionHandler()
                .handleUnexpected(new IllegalStateException("connection pool exhausted"));

        assertThat(problem.getStatus()).isEqualTo(500);
        assertThat(problem.getDetail()).isEqualTo("An unexpected error occurred.");
    }
}
