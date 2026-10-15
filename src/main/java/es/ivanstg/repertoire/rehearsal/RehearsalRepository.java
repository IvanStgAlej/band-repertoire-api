package es.ivanstg.repertoire.rehearsal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RehearsalRepository extends JpaRepository<Rehearsal, Long> {

    Page<Rehearsal> findByEnsembleId(Long ensembleId, Pageable pageable);
}
