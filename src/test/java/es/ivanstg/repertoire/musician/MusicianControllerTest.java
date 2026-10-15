package es.ivanstg.repertoire.musician;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.ivanstg.repertoire.ApiTest;

import org.junit.jupiter.api.Test;

class MusicianControllerTest extends ApiTest {

    @Test
    void createsAMusicianWithANormalisedEmail() throws Exception {
        long id = createId("/api/musicians", """
                {"name": "Ana Torres", "email": "  Ana.Torres@Example.com ", "mainInstrument": "Trumpet"}""");

        mvc.perform(get("/api/musicians/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana Torres"))
                .andExpect(jsonPath("$.email").value("ana.torres@example.com"))
                .andExpect(jsonPath("$.mainInstrument").value("Trumpet"));
    }

    @Test
    void listsMusiciansAsAPage() throws Exception {
        createMusician("Ben Ruiz", "ben@example.com");
        createMusician("Ana Torres", "ana@example.com");

        mvc.perform(get("/api/musicians"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Ana Torres"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void rejectsInvalidEmail() throws Exception {
        postJson("/api/musicians", """
                {"name": "Ana", "email": "not-an-email", "mainInstrument": "Trumpet"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void rejectsDuplicateEmailIgnoringCase() throws Exception {
        createMusician("Ana Torres", "ana@example.com");

        postJson("/api/musicians", """
                {"name": "Another Ana", "email": "ANA@example.com", "mainInstrument": "Flute"}""")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("A musician with email ana@example.com already exists"));
    }

    @Test
    void updatingToAnotherMusiciansEmailIs409ButKeepingYourOwnIsFine() throws Exception {
        createMusician("Ana Torres", "ana@example.com");
        long ben = createMusician("Ben Ruiz", "ben@example.com");

        putJson("/api/musicians/" + ben, """
                {"name": "Ben Ruiz", "email": "ana@example.com", "mainInstrument": "Tuba"}""")
                .andExpect(status().isConflict());
        putJson("/api/musicians/" + ben, """
                {"name": "Ben Ruiz", "email": "ben@example.com", "mainInstrument": "Tuba"}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mainInstrument").value("Tuba"));
    }

    @Test
    void deletesAMusician() throws Exception {
        long id = createMusician("Ana Torres", "ana@example.com");

        mvc.perform(delete("/api/musicians/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/musicians/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Musician " + id + " not found"));
    }
}
