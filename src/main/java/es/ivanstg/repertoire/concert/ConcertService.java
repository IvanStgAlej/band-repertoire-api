package es.ivanstg.repertoire.concert;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import es.ivanstg.repertoire.common.ConflictException;
import es.ivanstg.repertoire.common.NotFoundException;
import es.ivanstg.repertoire.common.PageResponse;
import es.ivanstg.repertoire.concert.dto.ConcertRequest;
import es.ivanstg.repertoire.concert.dto.ConcertResponse;
import es.ivanstg.repertoire.concert.dto.ProgramRequest;
import es.ivanstg.repertoire.ensemble.Ensemble;
import es.ivanstg.repertoire.ensemble.EnsembleService;
import es.ivanstg.repertoire.piece.Piece;
import es.ivanstg.repertoire.piece.PieceService;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConcertService {

    private final ConcertRepository repository;
    private final EnsembleService ensembleService;
    private final PieceService pieceService;

    public ConcertService(ConcertRepository repository, EnsembleService ensembleService, PieceService pieceService) {
        this.repository = repository;
        this.ensembleService = ensembleService;
        this.pieceService = pieceService;
    }

    public ConcertResponse create(Long ensembleId, ConcertRequest request) {
        Ensemble ensemble = ensembleService.find(ensembleId);
        Concert concert = new Concert(ensemble, request.title(), request.date(), request.venue());
        return ConcertResponse.from(repository.save(concert));
    }

    @Transactional(readOnly = true)
    public ConcertResponse get(Long id) {
        return ConcertResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<ConcertResponse> list(Long ensembleId, Pageable pageable) {
        ensembleService.find(ensembleId);
        return PageResponse.from(repository.findByEnsembleId(ensembleId, pageable).map(ConcertResponse::from));
    }

    public ConcertResponse replaceProgram(Long concertId, ProgramRequest request) {
        Concert concert = find(concertId);
        Long ensembleId = concert.getEnsemble().getId();
        Set<Long> seen = new HashSet<>();
        for (Long pieceId : request.pieceIds()) {
            if (!seen.add(pieceId)) {
                throw new ConflictException("Piece " + pieceId + " appears more than once in the program");
            }
        }
        List<Piece> pieces = request.pieceIds().stream()
                .map(pieceId -> pieceService.findInRepertoire(ensembleId, pieceId))
                .toList();

        // Hibernate runs inserts before deletes when it flushes. Without this explicit flush, re-adding a piece
        // that was already in the program would break the (concert, piece) and (concert, position) unique keys.
        concert.clearProgram();
        repository.flush();
        concert.addToProgram(pieces);
        return ConcertResponse.from(repository.saveAndFlush(concert));
    }

    @Transactional(readOnly = true)
    public Concert find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Concert", id));
    }
}
