package es.ivanstg.repertoire.rehearsal.dto;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** {@code pieceIds} lists the pieces worked on; it may be omitted. */
public record RehearsalRequest(
        @Schema(example = "2026-10-22T19:30:00") @NotNull LocalDateTime startsAt,
        @Schema(example = "Music school") @Size(max = 160) String location,
        @Schema(example = "Bring the new parts") @Size(max = 1000) String notes,
        @Schema(example = "[1, 2, 5]", description = "Pieces worked on")
        List<@NotNull Long> pieceIds) {

    public RehearsalRequest {
        pieceIds = pieceIds == null ? List.of() : List.copyOf(pieceIds);
    }
}
