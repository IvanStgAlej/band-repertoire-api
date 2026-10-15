package es.ivanstg.repertoire;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for HTTP-level tests: full application context, real H2 database, every test rolled back.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class ApiTest {

    @Autowired
    protected MockMvc mvc;

    protected ResultActions postJson(String url, String json) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    protected ResultActions putJson(String url, String json) throws Exception {
        return mvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    /** POSTs the body, expects 201 Created and returns the new resource id. */
    protected long createId(String url, String json) throws Exception {
        String body = postJson(url, json).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.<Number>read(body, "$.id").longValue();
    }

    protected long createEnsemble(String name) throws Exception {
        return createId("/api/ensembles", """
                {"name": "%s", "type": "BAND"}""".formatted(name));
    }

    protected long createMusician(String name, String email) throws Exception {
        return createId("/api/musicians", """
                {"name": "%s", "email": "%s", "mainInstrument": "Alto saxophone"}""".formatted(name, email));
    }

    protected void addMember(long ensembleId, long musicianId) throws Exception {
        postJson("/api/ensembles/" + ensembleId + "/members", """
                {"musicianId": %d, "instrument": "Alto saxophone"}""".formatted(musicianId))
                .andExpect(status().isCreated());
    }

    /** {@code startsAt} is an ISO local date-time such as "2026-10-01T19:30:00". */
    protected long createRehearsal(long ensembleId, String startsAt, long... pieceIds) throws Exception {
        String ids = java.util.Arrays.stream(pieceIds).mapToObj(String::valueOf)
                .collect(java.util.stream.Collectors.joining(", "));
        return createId("/api/ensembles/" + ensembleId + "/rehearsals", """
                {"startsAt": "%s", "location": "Music school", "pieceIds": [%s]}""".formatted(startsAt, ids));
    }

    protected long createPiece(long ensembleId, String title, int durationSeconds, int difficulty) throws Exception {
        return createId("/api/ensembles/" + ensembleId + "/pieces", """
                {"title": "%s", "durationSeconds": %d, "difficulty": %d}"""
                .formatted(title, durationSeconds, difficulty));
    }
}
