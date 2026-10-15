package es.ivanstg.repertoire.concert;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.ivanstg.repertoire.ApiTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConcertControllerTest extends ApiTest {

    private long band;
    private long arsenal;
    private long caminoReal;
    private long amparitoRoca;

    @BeforeEach
    void setUpRepertoire() throws Exception {
        band = createEnsemble("Riverside Concert Band");
        arsenal = createPiece(band, "Arsenal", 240, 3);
        caminoReal = createPiece(band, "El Camino Real", 660, 4);
        amparitoRoca = createPiece(band, "Amparito Roca", 180, 2);
    }

    @Test
    void createsAConcertWithAnEmptyProgram() throws Exception {
        long id = createConcert(band, "Autumn Concert", "2026-11-01");

        mvc.perform(get("/api/concerts/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ensembleId").value(band))
                .andExpect(jsonPath("$.title").value("Autumn Concert"))
                .andExpect(jsonPath("$.date").value("2026-11-01"))
                .andExpect(jsonPath("$.venue").value("Town hall"))
                .andExpect(jsonPath("$.program", hasSize(0)));
    }

    @Test
    void rejectsMissingTitleAndDate() throws Exception {
        postJson("/api/ensembles/" + band + "/concerts", """
                {"venue": "Town hall"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.date").exists());
    }

    @Test
    void listsTheConcertsOfAnEnsembleByDate() throws Exception {
        createConcert(band, "Spring Concert", "2027-04-10");
        createConcert(band, "Autumn Concert", "2026-11-01");

        mvc.perform(get("/api/ensembles/" + band + "/concerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].title", contains("Autumn Concert", "Spring Concert")));
    }

    @Test
    void setsAnOrderedProgram() throws Exception {
        long concert = createConcert(band, "Autumn Concert", "2026-11-01");

        setProgram(concert, caminoReal, arsenal, amparitoRoca)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.program[*].title", contains("El Camino Real", "Arsenal", "Amparito Roca")))
                .andExpect(jsonPath("$.program[*].position", contains(1, 2, 3)))
                .andExpect(jsonPath("$.program[0].durationSeconds").value(660))
                .andExpect(jsonPath("$.program[0].difficulty").value(4));
    }

    @Test
    void repeatingAPieceInTheProgramIs409() throws Exception {
        long concert = createConcert(band, "Autumn Concert", "2026-11-01");

        setProgram(concert, arsenal, caminoReal, arsenal)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Piece " + arsenal + " appears more than once in the program"));
    }

    @Test
    void addingAPieceFromAnotherEnsembleIs422() throws Exception {
        long bigBand = createEnsemble("Night Owls Big Band");
        long inTheMood = createPiece(bigBand, "In the Mood", 200, 2);
        long concert = createConcert(band, "Autumn Concert", "2026-11-01");

        setProgram(concert, arsenal, inTheMood)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail")
                        .value("Piece " + inTheMood + " is not in the repertoire of ensemble " + band));
    }

    @Test
    void addingAPieceThatDoesNotExistIs422() throws Exception {
        long concert = createConcert(band, "Autumn Concert", "2026-11-01");

        setProgram(concert, 999999)
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void replacingAProgramWithOverlappingPiecesWorks() throws Exception {
        long concert = createConcert(band, "Autumn Concert", "2026-11-01");
        setProgram(concert, arsenal, caminoReal, amparitoRoca).andExpect(status().isOk());

        setProgram(concert, amparitoRoca, arsenal)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.program[*].title", contains("Amparito Roca", "Arsenal")));
        mvc.perform(get("/api/concerts/" + concert))
                .andExpect(jsonPath("$.program[*].title", contains("Amparito Roca", "Arsenal")))
                .andExpect(jsonPath("$.program[*].position", contains(1, 2)));
    }

    @Test
    void emptyListClearsTheProgram() throws Exception {
        long concert = createConcert(band, "Autumn Concert", "2026-11-01");
        setProgram(concert, arsenal, caminoReal).andExpect(status().isOk());

        setProgram(concert)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.program", hasSize(0)));
    }

    @Test
    void missingPieceIdsListIs400() throws Exception {
        long concert = createConcert(band, "Autumn Concert", "2026-11-01");

        putJson("/api/concerts/" + concert + "/program", "{}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void summaryEndpointReturnsTotalsAndWarnings() throws Exception {
        long concert = createConcert(band, "Autumn Concert", "2026-11-01");
        setProgram(concert, caminoReal, arsenal, amparitoRoca).andExpect(status().isOk());
        createRehearsal(band, "2026-10-25T19:30:00", caminoReal, arsenal);
        createRehearsal(band, "2026-09-20T19:30:00", amparitoRoca);

        mvc.perform(get("/api/concerts/" + concert + "/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Autumn Concert"))
                .andExpect(jsonPath("$.pieceCount").value(3))
                .andExpect(jsonPath("$.totalDurationSeconds").value(1080))
                .andExpect(jsonPath("$.totalDuration").value("18:00"))
                .andExpect(jsonPath("$.averageDifficulty").value(3.0))
                .andExpect(jsonPath("$.warnings", hasSize(1)))
                .andExpect(jsonPath("$.warnings[0].title").value("Amparito Roca"))
                .andExpect(jsonPath("$.warnings[0].lastRehearsedAt").value("2026-09-20T19:30:00"));
    }

    @Test
    void aRehearsalOnTheConcertDayDoesNotCountAsPreparation() throws Exception {
        long concert = createConcert(band, "Autumn Concert", "2026-11-01");
        setProgram(concert, arsenal).andExpect(status().isOk());
        createRehearsal(band, "2026-11-01T10:00:00", arsenal);

        mvc.perform(get("/api/concerts/" + concert + "/summary"))
                .andExpect(jsonPath("$.warnings", hasSize(1)))
                .andExpect(jsonPath("$.warnings[0].message").value("Never rehearsed before the concert"));
    }

    @Test
    void summaryOfAnEmptyProgramHasNoAverage() throws Exception {
        long concert = createConcert(band, "Autumn Concert", "2026-11-01");

        mvc.perform(get("/api/concerts/" + concert + "/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pieceCount").value(0))
                .andExpect(jsonPath("$.averageDifficulty").doesNotExist())
                .andExpect(jsonPath("$.warnings", hasSize(0)));
    }

    @Test
    void unknownConcertIs404() throws Exception {
        mvc.perform(get("/api/concerts/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Concert 999999 not found"));
        setProgram(999999, arsenal).andExpect(status().isNotFound());
    }
}
