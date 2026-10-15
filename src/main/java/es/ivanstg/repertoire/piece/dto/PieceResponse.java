package es.ivanstg.repertoire.piece.dto;

import es.ivanstg.repertoire.piece.Piece;

public record PieceResponse(Long id, Long ensembleId, String title, String composer, String arranger,
                            int durationSeconds, int difficulty, String genre) {

    public static PieceResponse from(Piece piece) {
        return new PieceResponse(piece.getId(), piece.getEnsemble().getId(), piece.getTitle(), piece.getComposer(),
                piece.getArranger(), piece.getDurationSeconds(), piece.getDifficulty(), piece.getGenre());
    }
}
