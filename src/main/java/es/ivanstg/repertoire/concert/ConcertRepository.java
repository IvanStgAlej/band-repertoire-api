package es.ivanstg.repertoire.concert;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertRepository extends JpaRepository<Concert, Long> {

    Page<Concert> findByEnsembleId(Long ensembleId, Pageable pageable);
}
