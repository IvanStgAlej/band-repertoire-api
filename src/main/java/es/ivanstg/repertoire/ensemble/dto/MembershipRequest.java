package es.ivanstg.repertoire.ensemble.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** {@code joinedOn} is optional and defaults to today. */
public record MembershipRequest(
        @NotNull Long musicianId,
        @NotBlank @Size(max = 60) String instrument,
        LocalDate joinedOn) {
}
