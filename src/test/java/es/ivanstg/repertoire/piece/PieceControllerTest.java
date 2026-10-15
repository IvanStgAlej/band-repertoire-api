package es.ivanstg.repertoire.piece;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.ivanstg.repertoire.ApiTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PieceControllerTest extends ApiTest {

    private long band;
    private long bigBand;

    @BeforeEach
    void createEnsembles() throws Exception {
        band = createEnsemble("Riverside Concert Band");
        bigBand = createEnsemble("Night Owls Big Band");
    }

    private long createFullPiece(long ensembleId, String title, String composer, int difficulty, String genre)
            throws Exception {
        return createId("/api/ensembles/" + ensembleId + "/pieces", """
                {"title": "%s", "composer": "%s", "durationSeconds": 300, "difficulty": %d, "genre": "%s"}"""
                .formatted(title, composer, difficulty, genre));
    }

    @Test
    void createsAPieceInTheRepertoire() throws Exception {
        long id = createId("/api/ensembles/" + band + "/pieces", """
                {"title": "El Camino Real", "composer": "Alfred Reed", "arranger": null,
                 "durationSeconds": 660, "difficulty": 4, "genre": "Concert band"}""");

        mvc.perform(get("/api/pieces/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ensembleId").value(band))
                .andExpect(jsonPath("$.title").value("El Camino Real"))
                .andExpect(jsonPath("$.durationSeconds").value(660))
                .andExpect(jsonPath("$.difficulty").value(4));
    }

    @Test
    void listsTheRepertoireOfOneEnsemble() throws Exception {
        createPiece(band, "Arsenal", 240, 3);
        createPiece(bigBand, "Take the A Train", 200, 2);

        mvc.perform(get("/api/ensembles/" + band + "/pieces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Arsenal"));
    }

    @Test
    void creatingAPieceForAnUnknownEnsembleIs404() throws Exception {
        postJson("/api/ensembles/999999/pieces", """
                {"title": "Arsenal", "durationSeconds": 240, "difficulty": 3}""")
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsZeroDurationAndDifficultyOutOfRange() throws Exception {
        postJson("/api/ensembles/" + band + "/pieces", """
                {"title": "Arsenal", "durationSeconds": 0, "difficulty": 6}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.durationSeconds").exists())
                .andExpect(jsonPath("$.errors.difficulty").exists());
    }

    @Test
    void updatesAPiece() throws Exception {
        long id = createPiece(band, "Arsenal", 240, 3);

        putJson("/api/pieces/" + id, """
                {"title": "Arsenal", "composer": "Jan Van der Roost", "durationSeconds": 250, "difficulty": 4}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.composer").value("Jan Van der Roost"))
                .andExpect(jsonPath("$.durationSeconds").value(250));
    }

    @Test
    void deletesAPiece() throws Exception {
        long id = createPiece(band, "Arsenal", 240, 3);

        mvc.perform(delete("/api/pieces/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/pieces/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Piece " + id + " not found"));
    }

    @Test
    void deletingAPieceInAConcertProgramIs409() throws Exception {
        long id = createPiece(band, "Arsenal", 240, 3);
        long concert = createConcert(band, "Autumn Concert", "2026-11-01");
        setProgram(concert, id).andExpect(status().isOk());

        mvc.perform(delete("/api/pieces/" + id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value("Piece " + id + " is part of a concert program; remove it from the program first"));
        mvc.perform(get("/api/pieces/" + id)).andExpect(status().isOk());
    }

    @Test
    void deletingAnEnsembleRemovesItsRepertoireAndConcerts() throws Exception {
        long id = createPiece(band, "Arsenal", 240, 3);
        long concert = createConcert(band, "Autumn Concert", "2026-11-01");
        setProgram(concert, id).andExpect(status().isOk());

        flushAndClear();
        mvc.perform(delete("/api/ensembles/" + band)).andExpect(status().isNoContent());
        flushAndClear();
        mvc.perform(get("/api/pieces/" + id)).andExpect(status().isNotFound());
        mvc.perform(get("/api/concerts/" + concert)).andExpect(status().isNotFound());
    }

    @Test
    void searchesByTitleOrComposerIgnoringCase() throws Exception {
        createFullPiece(band, "El Camino Real", "Alfred Reed", 4, "Concert band");
        createFullPiece(band, "Arsenal", "Jan Van der Roost", 3, "March");

        mvc.perform(get("/api/pieces?q=REED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("El Camino Real"));
        mvc.perform(get("/api/pieces?q=arsen"))
                .andExpect(jsonPath("$.content[0].title").value("Arsenal"));
    }

    @Test
    void filtersByEnsembleMaxDifficultyAndGenre() throws Exception {
        createFullPiece(band, "El Camino Real", "Alfred Reed", 4, "Concert band");
        createFullPiece(band, "Arsenal", "Jan Van der Roost", 3, "March");
        createFullPiece(band, "Amparito Roca", "Jaime Texidor", 2, "march");
        createFullPiece(bigBand, "In the Mood", "Joe Garland", 2, "Swing");

        mvc.perform(get("/api/pieces?ensembleId=" + band + "&maxDifficulty=3"))
                .andExpect(jsonPath("$.content[*].title", containsInAnyOrder("Amparito Roca", "Arsenal")));
        mvc.perform(get("/api/pieces?genre=MARCH"))
                .andExpect(jsonPath("$.content[*].title", containsInAnyOrder("Amparito Roca", "Arsenal")));
        mvc.perform(get("/api/pieces?maxDifficulty=2&genre=swing"))
                .andExpect(jsonPath("$.content[*].title", containsInAnyOrder("In the Mood")));
    }

    @Test
    void searchWithoutFiltersReturnsEverythingSortedByTitle() throws Exception {
        createPiece(band, "Arsenal", 240, 3);
        createPiece(bigBand, "In the Mood", 200, 2);

        mvc.perform(get("/api/pieces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Arsenal"))
                .andExpect(jsonPath("$.content[1].title").value("In the Mood"));
    }

    @Test
    void rejectsMaxDifficultyOutOfRange() throws Exception {
        mvc.perform(get("/api/pieces?maxDifficulty=9")).andExpect(status().isBadRequest());
    }
}
