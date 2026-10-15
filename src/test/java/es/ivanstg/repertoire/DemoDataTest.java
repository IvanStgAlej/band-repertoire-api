package es.ivanstg.repertoire;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Runs with the default profile, so the demo migration is applied on top of the schema. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DemoDataTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void demoEnsemblesAndRepertoireAreLoaded() throws Exception {
        mvc.perform(get("/api/ensembles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].name", hasItems("Riverside Concert Band", "Night Owls Big Band")));
        mvc.perform(get("/api/ensembles/1/pieces"))
                .andExpect(jsonPath("$.totalElements").value(15));
        mvc.perform(get("/api/ensembles/1/members"))
                .andExpect(jsonPath("$", hasSize(8)));
    }

    @Test
    void demoConcertSummaryWarnsAboutTwoPieces() throws Exception {
        mvc.perform(get("/api/concerts/1/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Autumn Concert"))
                .andExpect(jsonPath("$.pieceCount").value(6))
                .andExpect(jsonPath("$.warnings[*].title", contains("Amparito Roca", "First Suite in E-flat")));
    }

    @Test
    void demoDataFeedsTheStaleAndAttendanceReports() throws Exception {
        mvc.perform(get("/api/ensembles/1/pieces/stale?weeks=4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(8)));
        mvc.perform(get("/api/ensembles/1/attendance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Hugo Castro"))
                .andExpect(jsonPath("$[0].attendanceRate").value(20.0));
    }

    @Test
    void newRowsDoNotCollideWithDemoIds() throws Exception {
        mvc.perform(post("/api/ensembles").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "New Combo", "type": "COMBO"}"""))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/musicians").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "New Player", "email": "new@example.com", "mainInstrument": "Piano"}"""))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/ensembles/1/pieces").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "New Piece", "durationSeconds": 120, "difficulty": 1}"""))
                .andExpect(status().isCreated());
    }
}
