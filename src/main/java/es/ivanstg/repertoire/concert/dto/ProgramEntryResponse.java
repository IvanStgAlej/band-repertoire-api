package es.ivanstg.repertoire.concert.dto;

import es.ivanstg.repertoire.concert.ProgramItem;

public record ProgramEntryResponse(int position, Long pieceId, String title, int durationSeconds, int difficulty) {

    public static ProgramEntryResponse from(ProgramItem item) {
        var piece = item.getPiece();
        return new ProgramEntryResponse(item.getSortOrder(), piece.getId(), piece.getTitle(),
                piece.getDurationSeconds(), piece.getDifficulty());
    }
}
