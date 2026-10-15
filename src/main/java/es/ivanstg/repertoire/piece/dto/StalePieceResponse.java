package es.ivanstg.repertoire.piece.dto;

import java.time.LocalDateTime;

/** {@code lastRehearsedAt} and {@code weeksSinceLastRehearsal} are null when the piece was never rehearsed. */
public record StalePieceResponse(Long pieceId, String title, LocalDateTime lastRehearsedAt,
                                 Long weeksSinceLastRehearsal) {
}
