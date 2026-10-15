package es.ivanstg.repertoire.ensemble.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** {@code joinedOn} is optional and defaults to today. */
public record MembershipRequest(
        @Schema(example = "3") @NotNull Long musicianId,
        @Schema(example = "First alto saxophone") @NotBlank @Size(max = 60) String instrument,
        @Schema(example = "2013-09-20", description = "Defaults to today") LocalDate joinedOn) {
}
