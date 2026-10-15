package es.ivanstg.repertoire.concert.dto;

import java.time.LocalDate;
import java.util.List;

/** {@code averageDifficulty} is null for an empty program. */
public record ConcertSummaryResponse(Long concertId, String title, LocalDate date, int pieceCount,
                                     int totalDurationSeconds, String totalDuration, Double averageDifficulty,
                                     List<PreparationWarning> warnings) {
}
