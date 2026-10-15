package es.ivanstg.repertoire.ensemble;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    boolean existsByEnsembleIdAndMusicianId(Long ensembleId, Long musicianId);

    Optional<Membership> findByEnsembleIdAndMusicianId(Long ensembleId, Long musicianId);

    @EntityGraph(attributePaths = "musician")
    List<Membership> findByEnsembleIdOrderByMusicianNameAsc(Long ensembleId);
}
