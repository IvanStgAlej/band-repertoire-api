package es.ivanstg.repertoire.concert.dto;

import java.time.LocalDateTime;

public record PreparationWarning(Long pieceId, String title, LocalDateTime lastRehearsedAt, String message) {
}
