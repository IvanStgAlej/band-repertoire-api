package es.ivanstg.repertoire.musician.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MusicianRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Email @Size(max = 180) String email,
        @NotBlank @Size(max = 60) String mainInstrument) {

    /** Bean validation runs before the service strips the email, so accept surrounding spaces here. */
    public MusicianRequest {
        if (email != null) {
            email = email.strip();
        }
    }
}
