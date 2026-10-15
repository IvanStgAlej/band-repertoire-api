package es.ivanstg.repertoire.rehearsal;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.ivanstg.repertoire.ApiTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RehearsalControllerTest extends ApiTest {

    private long band;
    private long arsenal;
    private long caminoReal;
    private long ana;
    private long ben;

    @BeforeEach
    void setUpBand() throws Exception {
        band = createEnsemble("Riverside Concert Band");
        arsenal = createPiece(band, "Arsenal", 240, 3);
        caminoReal = createPiece(band, "El Camino Real", 660, 4);
        ana = createMusician("Ana Torres", "ana@example.com");
        ben = createMusician("Ben Ruiz", "ben@example.com");
        addMember(band, ana);
        addMember(band, ben);
    }

    @Test
    void createsARehearsalWithRehearsedPieces() throws Exception {
        long id = createRehearsal(band, "2026-10-01T19:30:00", arsenal, caminoReal);

        mvc.perform(get("/api/rehearsals/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ensembleId").value(band))
                .andExpect(jsonPath("$.startsAt").value("2026-10-01T19:30:00"))
                .andExpect(jsonPath("$.location").value("Music school"))
                .andExpect(jsonPath("$.pieces[*].title", containsInAnyOrder("Arsenal", "El Camino Real")))
                .andExpect(jsonPath("$.attendance", hasSize(0)));
    }

    @Test
    void pieceIdsAreOptional() throws Exception {
        postJson("/api/ensembles/" + band + "/rehearsals", """
                {"startsAt": "2026-10-01T19:30:00"}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pieces", hasSize(0)));
    }

    @Test
    void rejectsMissingStartTime() throws Exception {
        postJson("/api/ensembles/" + band + "/rehearsals", """
                {"location": "Music school"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.startsAt").exists());
    }

    @Test
    void rehearsingAPieceFromAnotherEnsembleIs422() throws Exception {
        long bigBand = createEnsemble("Night Owls Big Band");
        long inTheMood = createPiece(bigBand, "In the Mood", 200, 2);

        postJson("/api/ensembles/" + band + "/rehearsals", """
                {"startsAt": "2026-10-01T19:30:00", "pieceIds": [%d]}""".formatted(inTheMood))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail")
                        .value("Piece " + inTheMood + " is not in the repertoire of ensemble " + band));
    }

    @Test
    void recordsAttendanceForMembersAndUpdatesInsteadOfDuplicating() throws Exception {
        long rehearsal = createRehearsal(band, "2026-10-01T19:30:00", arsenal);

        putJson("/api/rehearsals/" + rehearsal + "/attendance", """
                {"entries": [
                  {"musicianId": %d, "attended": true},
                  {"musicianId": %d, "attended": false, "reason": "Sick"}
                ]}""".formatted(ana, ben))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attendance", hasSize(2)));

        putJson("/api/rehearsals/" + rehearsal + "/attendance", """
                {"entries": [{"musicianId": %d, "attended": true}]}""".formatted(ben))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attendance", hasSize(2)))
                .andExpect(jsonPath("$.attendance[?(@.name == 'Ben Ruiz')].attended").value(true))
                .andExpect(jsonPath("$.attendance[?(@.name == 'Ana Torres')].attended").value(true));
    }

    @Test
    void attendanceForANonMemberIs422() throws Exception {
        long rehearsal = createRehearsal(band, "2026-10-01T19:30:00");
        long outsider = createMusician("Cleo Diaz", "cleo@example.com");

        putJson("/api/rehearsals/" + rehearsal + "/attendance", """
                {"entries": [{"musicianId": %d, "attended": true}]}""".formatted(outsider))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail")
                        .value("Musician " + outsider + " is not a member of ensemble " + band));
    }

    @Test
    void duplicateMusicianInOneAttendanceRequestIs400() throws Exception {
        long rehearsal = createRehearsal(band, "2026-10-01T19:30:00");

        putJson("/api/rehearsals/" + rehearsal + "/attendance", """
                {"entries": [
                  {"musicianId": %d, "attended": true},
                  {"musicianId": %d, "attended": false}
                ]}""".formatted(ana, ana))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Musician " + ana + " appears more than once"));
    }

    @Test
    void emptyAttendanceRequestIs400() throws Exception {
        long rehearsal = createRehearsal(band, "2026-10-01T19:30:00");

        putJson("/api/rehearsals/" + rehearsal + "/attendance", """
                {"entries": []}""")
                .andExpect(status().isBadRequest());
    }

    @Test
    void listsRehearsalsNewestFirst() throws Exception {
        createRehearsal(band, "2026-09-01T19:30:00");
        createRehearsal(band, "2026-10-01T19:30:00");

        mvc.perform(get("/api/ensembles/" + band + "/rehearsals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].startsAt").value("2026-10-01T19:30:00"))
                .andExpect(jsonPath("$.content[1].startsAt").value("2026-09-01T19:30:00"));
    }

    @Test
    void unknownRehearsalIs404() throws Exception {
        mvc.perform(get("/api/rehearsals/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Rehearsal 999999 not found"));
    }
}
