package es.ivanstg.repertoire.ensemble.dto;

import es.ivanstg.repertoire.ensemble.EnsembleType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EnsembleRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull EnsembleType type,
        @Size(max = 120) String city) {
}
