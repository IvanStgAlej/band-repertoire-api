package es.ivanstg.repertoire.musician.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MusicianRequest(
        @Schema(example = "Lucia Moreno") @NotBlank @Size(max = 120) String name,
        @Schema(example = "lucia.moreno@example.com") @NotBlank @Email @Size(max = 180) String email,
        @Schema(example = "Flute") @NotBlank @Size(max = 60) String mainInstrument) {

    /** Bean validation runs before the service strips the email, so accept surrounding spaces here. */
    public MusicianRequest {
        if (email != null) {
            email = email.strip();
        }
    }
}
