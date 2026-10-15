package es.ivanstg.repertoire.piece;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface PieceRepository extends JpaRepository<Piece, Long>, JpaSpecificationExecutor<Piece> {

    Page<Piece> findByEnsembleId(Long ensembleId, Pageable pageable);

    @Query("select count(pi) > 0 from ProgramItem pi where pi.piece.id = :pieceId")
    boolean isInAnyProgram(Long pieceId);
}
