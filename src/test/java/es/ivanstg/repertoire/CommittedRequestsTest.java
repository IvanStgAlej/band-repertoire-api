package es.ivanstg.repertoire;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Program replacement and ensemble deletion with every request committed, as in production. */
class CommittedRequestsTest extends CommittedApiTest {

    private long band;
    private long arsenal;
    private long caminoReal;
    private long amparitoRoca;
    private long concert;

    @BeforeEach
    void setUpConcert() throws Exception {
        band = createEnsemble("Riverside Concert Band");
        arsenal = createPiece(band, "Arsenal", 240, 3);
        caminoReal = createPiece(band, "El Camino Real", 660, 4);
        amparitoRoca = createPiece(band, "Amparito Roca", 180, 2);
        concert = createConcert(band, "Autumn Concert", "2026-11-01");
    }

    @Test
    void replacingAProgramWithOverlappingPiecesCommits() throws Exception {
        setProgram(concert, arsenal, caminoReal, amparitoRoca).andExpect(status().isOk());

        setProgram(concert, amparitoRoca, arsenal).andExpect(status().isOk());

        mvc.perform(get("/api/concerts/" + concert))
                .andExpect(jsonPath("$.program[*].title", contains("Amparito Roca", "Arsenal")))
                .andExpect(jsonPath("$.program[*].position", contains(1, 2)));
        assertThat(count("program_item")).isEqualTo(2);
    }

    @Test
    void aRejectedReplacementLeavesTheCommittedProgramUntouched() throws Exception {
        setProgram(concert, arsenal, caminoReal).andExpect(status().isOk());
        long bigBand = createEnsemble("Night Owls Big Band");
        long inTheMood = createPiece(bigBand, "In the Mood", 200, 2);

        setProgram(concert, amparitoRoca, inTheMood).andExpect(status().isUnprocessableContent());
        setProgram(concert, amparitoRoca, amparitoRoca).andExpect(status().isConflict());

        mvc.perform(get("/api/concerts/" + concert))
                .andExpect(jsonPath("$.program[*].title", contains("Arsenal", "El Camino Real")));
        assertThat(count("program_item")).isEqualTo(2);
    }

    @Test
    void deletingAnEnsembleCascadesThroughEverythingItOwns() throws Exception {
        long ana = createMusician("Ana Torres", "ana@example.com");
        addMember(band, ana);
        setProgram(concert, arsenal, caminoReal).andExpect(status().isOk());
        long rehearsal = createRehearsal(band, "2026-10-25T19:30:00", arsenal, amparitoRoca);
        putJson("/api/rehearsals/" + rehearsal + "/attendance", """
                {"entries": [{"musicianId": %d, "attended": true}]}""".formatted(ana))
                .andExpect(status().isOk());

        mvc.perform(delete("/api/ensembles/" + band)).andExpect(status().isNoContent());

        mvc.perform(get("/api/ensembles/" + band)).andExpect(status().isNotFound());
        mvc.perform(get("/api/concerts/" + concert)).andExpect(status().isNotFound());
        mvc.perform(get("/api/rehearsals/" + rehearsal)).andExpect(status().isNotFound());
        mvc.perform(get("/api/pieces/" + arsenal)).andExpect(status().isNotFound());
        for (String table : new String[] {"membership", "piece", "rehearsal", "rehearsal_piece", "attendance",
                "concert", "program_item"}) {
            assertThat(count(table)).as(table).isZero();
        }
        mvc.perform(get("/api/musicians/" + ana)).andExpect(status().isOk());
    }
}
