package es.ivanstg.repertoire;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.Test;

/** The spec asks for Swagger UI to document every endpoint with examples. */
class OpenApiDocsTest extends ApiTest {

    @Test
    void everyRequestFieldHasAnExample() throws Exception {
        String docs = mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map<String, Map<String, Object>> schemas = JsonPath.read(docs, "$.components.schemas");

        List<String> missing = new ArrayList<>();
        schemas.forEach((name, schema) -> {
            if (name.endsWith("Request") || name.endsWith("Entry")) {
                @SuppressWarnings("unchecked")
                Map<String, Map<String, Object>> properties = (Map<String, Map<String, Object>>) schema.get("properties");
                properties.forEach((property, definition) -> {
                    if (!definition.containsKey("example") && !definition.containsKey("$ref")
                            && !"array".equals(definition.get("type"))) {
                        missing.add(name + "." + property);
                    }
                });
            }
        });

        assertThat(schemas).containsKeys("EnsembleRequest", "PieceRequest", "ProgramRequest", "AttendanceEntry");
        assertThat(missing).as("request fields without an example").isEmpty();
    }

    @Test
    void listFieldsInRequestsHaveExamples() throws Exception {
        String docs = mvc.perform(get("/v3/api-docs")).andReturn().getResponse().getContentAsString();

        for (String schema : List.of("ProgramRequest", "RehearsalRequest")) {
            Map<String, Object> pieceIds = JsonPath.read(docs, "$.components.schemas." + schema + ".properties.pieceIds");
            assertThat(pieceIds.get("example")).as(schema + ".pieceIds = " + pieceIds).isInstanceOf(List.class);
        }
    }
}
