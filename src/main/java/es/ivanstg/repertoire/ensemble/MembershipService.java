package es.ivanstg.repertoire.ensemble;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import es.ivanstg.repertoire.common.ConflictException;
import es.ivanstg.repertoire.common.NotFoundException;
import es.ivanstg.repertoire.ensemble.dto.MemberResponse;
import es.ivanstg.repertoire.ensemble.dto.MembershipRequest;
import es.ivanstg.repertoire.musician.Musician;
import es.ivanstg.repertoire.musician.MusicianService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MembershipService {

    private final MembershipRepository repository;
    private final EnsembleService ensembleService;
    private final MusicianService musicianService;
    private final Clock clock;

    public MembershipService(MembershipRepository repository, EnsembleService ensembleService,
                             MusicianService musicianService, Clock clock) {
        this.repository = repository;
        this.ensembleService = ensembleService;
        this.musicianService = musicianService;
        this.clock = clock;
    }

    public MemberResponse add(Long ensembleId, MembershipRequest request) {
        Ensemble ensemble = ensembleService.find(ensembleId);
        Musician musician = musicianService.find(request.musicianId());
        if (repository.existsByEnsembleIdAndMusicianId(ensembleId, musician.getId())) {
            throw new ConflictException(
                    "Musician " + musician.getId() + " is already a member of ensemble " + ensembleId);
        }
        LocalDate joinedOn = request.joinedOn() != null ? request.joinedOn() : LocalDate.now(clock);
        Membership membership = new Membership(ensemble, musician, request.instrument(), joinedOn);
        return MemberResponse.from(repository.save(membership));
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> list(Long ensembleId) {
        ensembleService.find(ensembleId);
        return repository.findByEnsembleIdOrderByMusicianNameAsc(ensembleId).stream()
                .map(MemberResponse::from)
                .toList();
    }

    public void remove(Long ensembleId, Long musicianId) {
        ensembleService.find(ensembleId);
        Membership membership = repository.findByEnsembleIdAndMusicianId(ensembleId, musicianId)
                .orElseThrow(() -> new NotFoundException(
                        "Musician " + musicianId + " is not a member of ensemble " + ensembleId));
        repository.delete(membership);
    }
}
