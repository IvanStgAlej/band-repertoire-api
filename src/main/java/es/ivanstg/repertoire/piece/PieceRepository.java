package es.ivanstg.repertoire.piece;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import es.ivanstg.repertoire.piece.dto.PieceLastRehearsal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface PieceRepository extends JpaRepository<Piece, Long>, JpaSpecificationExecutor<Piece> {

    Page<Piece> findByEnsembleId(Long ensembleId, Pageable pageable);

    @Query("select count(pi) > 0 from ProgramItem pi where pi.piece.id = :pieceId")
    boolean isInAnyProgram(Long pieceId);

    /**
     * Pieces of the ensemble whose last rehearsal before {@code now} is older than {@code threshold}, or that
     * were never rehearsed. Never-rehearsed pieces come first, then the longest forgotten.
     */
    @Query("""
            select new es.ivanstg.repertoire.piece.dto.PieceLastRehearsal(p.id, p.title, max(r.startsAt))
            from Piece p left join p.rehearsals r on r.startsAt < :now
            where p.ensemble.id = :ensembleId
            group by p.id, p.title
            having max(r.startsAt) is null or max(r.startsAt) < :threshold
            order by case when max(r.startsAt) is null then 0 else 1 end, max(r.startsAt), p.title
            """)
    List<PieceLastRehearsal> findStale(Long ensembleId, LocalDateTime now, LocalDateTime threshold);

    /** The last rehearsal before {@code before} of each given piece; pieces never rehearsed get null. */
    @Query("""
            select new es.ivanstg.repertoire.piece.dto.PieceLastRehearsal(p.id, p.title, max(r.startsAt))
            from Piece p left join p.rehearsals r on r.startsAt < :before
            where p.id in :pieceIds
            group by p.id, p.title
            """)
    List<PieceLastRehearsal> findLastRehearsals(Collection<Long> pieceIds, LocalDateTime before);
}
