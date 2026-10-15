package es.ivanstg.repertoire.piece;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PieceRepository extends JpaRepository<Piece, Long>, JpaSpecificationExecutor<Piece> {

    Page<Piece> findByEnsembleId(Long ensembleId, Pageable pageable);
}
