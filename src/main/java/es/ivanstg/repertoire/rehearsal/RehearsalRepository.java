package es.ivanstg.repertoire.rehearsal;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RehearsalRepository extends JpaRepository<Rehearsal, Long> {

    Page<Rehearsal> findByEnsembleId(Long ensembleId, Pageable pageable);

    long countByEnsembleIdAndStartsAtGreaterThanEqualAndStartsAtLessThan(Long ensembleId, LocalDateTime from,
                                                                         LocalDateTime to);
}
