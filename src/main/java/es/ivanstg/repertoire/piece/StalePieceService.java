package es.ivanstg.repertoire.piece;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import es.ivanstg.repertoire.ensemble.EnsembleService;
import es.ivanstg.repertoire.piece.dto.StalePieceResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Finds repertoire that has not been rehearsed for a while. Only rehearsals that already happened count. */
@Service
@Transactional(readOnly = true)
public class StalePieceService {

    private final PieceRepository repository;
    private final EnsembleService ensembleService;
    private final Clock clock;

    public StalePieceService(PieceRepository repository, EnsembleService ensembleService, Clock clock) {
        this.repository = repository;
        this.ensembleService = ensembleService;
        this.clock = clock;
    }

    public List<StalePieceResponse> findStale(Long ensembleId, int weeks) {
        ensembleService.find(ensembleId);
        LocalDateTime now = LocalDateTime.now(clock);
        return repository.findStale(ensembleId, now, now.minusWeeks(weeks)).stream()
                .map(row -> new StalePieceResponse(row.pieceId(), row.title(), row.lastRehearsedAt(),
                        row.lastRehearsedAt() == null ? null : ChronoUnit.WEEKS.between(row.lastRehearsedAt(), now)))
                .toList();
    }
}
