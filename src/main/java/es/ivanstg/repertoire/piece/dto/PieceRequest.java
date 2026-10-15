package es.ivanstg.repertoire.piece.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PieceRequest(
        @Schema(example = "Festive Overture") @NotBlank @Size(max = 200) String title,
        @Schema(example = "Dmitri Shostakovich") @Size(max = 120) String composer,
        @Schema(example = "Donald Hunsberger") @Size(max = 120) String arranger,
        @Schema(example = "360") @NotNull @Positive Integer durationSeconds,
        @Schema(example = "5", description = "1 (easy) to 5 (very hard)") @NotNull @Min(1) @Max(5) Integer difficulty,
        @Schema(example = "Overture") @Size(max = 60) String genre) {
}
