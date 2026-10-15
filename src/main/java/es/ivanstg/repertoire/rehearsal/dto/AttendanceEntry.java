package es.ivanstg.repertoire.rehearsal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AttendanceEntry(
        @Schema(example = "4") @NotNull Long musicianId,
        @Schema(example = "false") @NotNull Boolean attended,
        @Schema(example = "Sick") @Size(max = 200) String reason) {
}
