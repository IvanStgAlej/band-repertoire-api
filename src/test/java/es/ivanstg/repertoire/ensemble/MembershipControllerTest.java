package es.ivanstg.repertoire.ensemble;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import es.ivanstg.repertoire.ApiTest;

import org.junit.jupiter.api.Test;

class MembershipControllerTest extends ApiTest {

    @Test
    void addsAMusicianAndListsMembers() throws Exception {
        long band = createEnsemble("Riverside Concert Band");
        long ana = createMusician("Ana Torres", "ana@example.com");

        postJson("/api/ensembles/" + band + "/members", """
                {"musicianId": %d, "instrument": "Second trumpet", "joinedOn": "2020-09-01"}""".formatted(ana))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.musicianId").value(ana))
                .andExpect(jsonPath("$.instrument").value("Second trumpet"));

        mvc.perform(get("/api/ensembles/" + band + "/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Ana Torres"))
                .andExpect(jsonPath("$[0].email").value("ana@example.com"))
                .andExpect(jsonPath("$[0].instrument").value("Second trumpet"))
                .andExpect(jsonPath("$[0].joinedOn").value("2020-09-01"));
    }

    @Test
    void joinedOnDefaultsToToday() throws Exception {
        long band = createEnsemble("Riverside Concert Band");
        long ana = createMusician("Ana Torres", "ana@example.com");

        postJson("/api/ensembles/" + band + "/members", """
                {"musicianId": %d, "instrument": "Trumpet"}""".formatted(ana))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.joinedOn").value(LocalDate.now().toString()));
    }

    @Test
    void addingTheSameMusicianTwiceIs409() throws Exception {
        long band = createEnsemble("Riverside Concert Band");
        long ana = createMusician("Ana Torres", "ana@example.com");
        String body = """
                {"musicianId": %d, "instrument": "Trumpet"}""".formatted(ana);

        postJson("/api/ensembles/" + band + "/members", body).andExpect(status().isCreated());
        postJson("/api/ensembles/" + band + "/members", body)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value("Musician " + ana + " is already a member of ensemble " + band));
    }

    @Test
    void addingAnUnknownMusicianIs404() throws Exception {
        long band = createEnsemble("Riverside Concert Band");

        postJson("/api/ensembles/" + band + "/members", """
                {"musicianId": 999999, "instrument": "Trumpet"}""")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Musician 999999 not found"));
    }

    @Test
    void addingToAnUnknownEnsembleIs404() throws Exception {
        long ana = createMusician("Ana Torres", "ana@example.com");

        postJson("/api/ensembles/999999/members", """
                {"musicianId": %d, "instrument": "Trumpet"}""".formatted(ana))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Ensemble 999999 not found"));
    }

    @Test
    void removesAMember() throws Exception {
        long band = createEnsemble("Riverside Concert Band");
        long ana = createMusician("Ana Torres", "ana@example.com");
        postJson("/api/ensembles/" + band + "/members", """
                {"musicianId": %d, "instrument": "Trumpet"}""".formatted(ana));

        mvc.perform(delete("/api/ensembles/" + band + "/members/" + ana)).andExpect(status().isNoContent());
        mvc.perform(get("/api/ensembles/" + band + "/members")).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void removingANonMemberIs404() throws Exception {
        long band = createEnsemble("Riverside Concert Band");
        long ana = createMusician("Ana Torres", "ana@example.com");

        mvc.perform(delete("/api/ensembles/" + band + "/members/" + ana))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail")
                        .value("Musician " + ana + " is not a member of ensemble " + band));
    }

    @Test
    void aMusicianCanBelongToTwoEnsembles() throws Exception {
        long band = createEnsemble("Riverside Concert Band");
        long bigBand = createEnsemble("Night Owls Big Band");
        long ana = createMusician("Ana Torres", "ana@example.com");

        postJson("/api/ensembles/" + band + "/members", """
                {"musicianId": %d, "instrument": "Trumpet"}""".formatted(ana))
                .andExpect(status().isCreated());
        postJson("/api/ensembles/" + bigBand + "/members", """
                {"musicianId": %d, "instrument": "Lead trumpet"}""".formatted(ana))
                .andExpect(status().isCreated());
    }

    @Test
    void rejectsMissingMusicianIdAndBlankInstrument() throws Exception {
        long band = createEnsemble("Riverside Concert Band");

        postJson("/api/ensembles/" + band + "/members", """
                {"instrument": " "}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.musicianId").exists())
                .andExpect(jsonPath("$.errors.instrument").exists());
    }
}
