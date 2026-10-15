package es.ivanstg.repertoire.piece.dto;

import java.time.LocalDateTime;

/** Query result: a piece and its most recent rehearsal before a cut-off ({@code null} when there is none). */
public record PieceLastRehearsal(Long pieceId, String title, LocalDateTime lastRehearsedAt) {
}
