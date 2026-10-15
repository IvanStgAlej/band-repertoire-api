package es.ivanstg.repertoire.concert.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/** The whole program, in performance order. It replaces the current one; an empty list clears it. */
public record ProgramRequest(
        @Schema(example = "[1, 2, 4, 8]", description = "Piece ids in performance order")
        @NotNull List<@NotNull Long> pieceIds) {
}
