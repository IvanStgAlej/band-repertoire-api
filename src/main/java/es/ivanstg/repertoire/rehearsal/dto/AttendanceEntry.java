package es.ivanstg.repertoire.rehearsal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AttendanceEntry(
        @NotNull Long musicianId,
        @NotNull Boolean attended,
        @Size(max = 200) String reason) {
}
