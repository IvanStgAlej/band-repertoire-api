package es.ivanstg.repertoire.concert.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ConcertRequest(
        @NotBlank @Size(max = 160) String title,
        @NotNull LocalDate date,
        @Size(max = 160) String venue) {
}
