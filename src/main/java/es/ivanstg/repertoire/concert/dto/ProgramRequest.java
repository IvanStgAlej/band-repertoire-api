package es.ivanstg.repertoire.concert.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

/** The whole program, in performance order. It replaces the current one; an empty list clears it. */
public record ProgramRequest(@NotNull List<@NotNull Long> pieceIds) {
}
