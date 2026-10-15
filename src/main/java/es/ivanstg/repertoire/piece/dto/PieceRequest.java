package es.ivanstg.repertoire.piece.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PieceRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 120) String composer,
        @Size(max = 120) String arranger,
        @NotNull @Positive Integer durationSeconds,
        @NotNull @Min(1) @Max(5) Integer difficulty,
        @Size(max = 60) String genre) {
}
