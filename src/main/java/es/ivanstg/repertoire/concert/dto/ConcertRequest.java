package es.ivanstg.repertoire.concert.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ConcertRequest(
        @Schema(example = "Spring Concert") @NotBlank @Size(max = 160) String title,
        @Schema(example = "2027-04-10") @NotNull LocalDate date,
        @Schema(example = "Palau de la Musica") @Size(max = 160) String venue) {
}
