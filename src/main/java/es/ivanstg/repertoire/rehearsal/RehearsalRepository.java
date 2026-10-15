package es.ivanstg.repertoire.rehearsal;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RehearsalRepository extends JpaRepository<Rehearsal, Long> {

    Page<Rehearsal> findByEnsembleId(Long ensembleId, Pageable pageable);

    /** Start times of the ensemble's rehearsals in [from, to). */
    @Query("""
            select r.startsAt from Rehearsal r
            where r.ensemble.id = :ensembleId and r.startsAt >= :from and r.startsAt < :to
            """)
    List<LocalDateTime> findStartTimes(Long ensembleId, LocalDateTime from, LocalDateTime to);
}
