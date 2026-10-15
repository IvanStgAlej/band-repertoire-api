package es.ivanstg.repertoire.piece;

import java.util.ArrayList;
import java.util.List;

import es.ivanstg.repertoire.piece.dto.PieceSearch;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

/** Builds the repertoire search query. Filters left empty are simply not applied. */
public final class PieceSpecifications {

    private PieceSpecifications() {
    }

    public static Specification<Piece> matching(PieceSearch search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search.ensembleId() != null) {
                predicates.add(cb.equal(root.get("ensemble").get("id"), search.ensembleId()));
            }
            if (search.maxDifficulty() != null) {
                predicates.add(cb.le(root.get("difficulty"), search.maxDifficulty()));
            }
            if (search.genre() != null && !search.genre().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("genre")), search.genre().strip().toLowerCase()));
            }
            if (search.q() != null && !search.q().isBlank()) {
                String pattern = "%" + search.q().strip().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("composer")), pattern)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
