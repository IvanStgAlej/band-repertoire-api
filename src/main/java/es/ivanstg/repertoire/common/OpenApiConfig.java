package es.ivanstg.repertoire.common;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI bandRepertoireOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Band Repertoire API")
                .description("Manage a music ensemble's musicians, repertoire, rehearsals with attendance "
                        + "and concert programs with preparation warnings.")
                .version("1.0.0")
                .license(new License().name("MIT")));
    }
}
