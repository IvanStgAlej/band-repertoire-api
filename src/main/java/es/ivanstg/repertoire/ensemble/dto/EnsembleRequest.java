package es.ivanstg.repertoire.ensemble.dto;

import es.ivanstg.repertoire.ensemble.EnsembleType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EnsembleRequest(
        @Schema(example = "Riverside Concert Band") @NotBlank @Size(max = 120) String name,
        @Schema(example = "BAND") @NotNull EnsembleType type,
        @Schema(example = "Valencia") @Size(max = 120) String city) {
}
