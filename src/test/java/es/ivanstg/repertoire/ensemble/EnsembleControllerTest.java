package es.ivanstg.repertoire.ensemble;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.ivanstg.repertoire.ApiTest;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class EnsembleControllerTest extends ApiTest {

    @Test
    void createsAndFetchesAnEnsemble() throws Exception {
        String location = postJson("/api/ensembles", """
                {"name": "Riverside Concert Band", "type": "BAND", "city": "Valencia"}""")
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Riverside Concert Band"))
                .andExpect(jsonPath("$.type").value("BAND"))
                .andExpect(jsonPath("$.city").value("Valencia"));
    }

    @Test
    void listsEnsemblesAsAPageSortedByName() throws Exception {
        createEnsemble("Night Owls Big Band");
        createEnsemble("Brass Collective");

        mvc.perform(get("/api/ensembles?size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Brass Collective"))
                .andExpect(jsonPath("$.content[1].name").value("Night Owls Big Band"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void returnsProblemDetailWhenEnsembleDoesNotExist() throws Exception {
        mvc.perform(get("/api/ensembles/999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Ensemble 999999 not found"));
    }

    @Test
    void rejectsBlankNameWithFieldErrors() throws Exception {
        postJson("/api/ensembles", """
                {"name": " ", "type": "BAND"}""")
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void rejectsUnknownEnumValueAndMalformedJson() throws Exception {
        postJson("/api/ensembles", """
                {"name": "Trio", "type": "TRIO"}""")
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        postJson("/api/ensembles", "{\"name\":")
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void rejectsNonNumericIdAndUnknownSortProperty() throws Exception {
        mvc.perform(get("/api/ensembles/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(get("/api/ensembles?sort=banana"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void updatesAndDeletesAnEnsemble() throws Exception {
        long id = createEnsemble("Old Name");

        putJson("/api/ensembles/" + id, """
                {"name": "New Name", "type": "ORCHESTRA"}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.type").value("ORCHESTRA"));
        mvc.perform(delete("/api/ensembles/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/ensembles/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void updatingOrDeletingAMissingEnsembleIs404() throws Exception {
        putJson("/api/ensembles/999999", """
                {"name": "Ghost", "type": "BAND"}""")
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/ensembles/999999")).andExpect(status().isNotFound());
    }
}
