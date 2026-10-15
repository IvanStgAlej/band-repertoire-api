package es.ivanstg.repertoire.piece.dto;

/** Optional repertoire filters; {@code q} matches title or composer, ignoring case. */
public record PieceSearch(Long ensembleId, Integer maxDifficulty, String genre, String q) {
}
