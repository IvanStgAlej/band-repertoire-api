package es.ivanstg.repertoire.piece;

import es.ivanstg.repertoire.common.BusinessRuleException;
import es.ivanstg.repertoire.common.ConflictException;
import es.ivanstg.repertoire.common.NotFoundException;
import es.ivanstg.repertoire.common.PageResponse;
import es.ivanstg.repertoire.ensemble.Ensemble;
import es.ivanstg.repertoire.ensemble.EnsembleService;
import es.ivanstg.repertoire.piece.dto.PieceRequest;
import es.ivanstg.repertoire.piece.dto.PieceResponse;
import es.ivanstg.repertoire.piece.dto.PieceSearch;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PieceService {

    private final PieceRepository repository;
    private final EnsembleService ensembleService;

    public PieceService(PieceRepository repository, EnsembleService ensembleService) {
        this.repository = repository;
        this.ensembleService = ensembleService;
    }

    public PieceResponse create(Long ensembleId, PieceRequest request) {
        Ensemble ensemble = ensembleService.find(ensembleId);
        Piece piece = new Piece(ensemble, request.title(), request.composer(), request.arranger(),
                request.durationSeconds(), request.difficulty(), request.genre());
        return PieceResponse.from(repository.save(piece));
    }

    @Transactional(readOnly = true)
    public PieceResponse get(Long id) {
        return PieceResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<PieceResponse> listByEnsemble(Long ensembleId, Pageable pageable) {
        ensembleService.find(ensembleId);
        return PageResponse.from(repository.findByEnsembleId(ensembleId, pageable).map(PieceResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<PieceResponse> search(PieceSearch search, Pageable pageable) {
        return PageResponse.from(
                repository.findAll(PieceSpecifications.matching(search), pageable).map(PieceResponse::from));
    }

    public PieceResponse update(Long id, PieceRequest request) {
        Piece piece = find(id);
        piece.update(request.title(), request.composer(), request.arranger(), request.durationSeconds(),
                request.difficulty(), request.genre());
        return PieceResponse.from(piece);
    }

    public void delete(Long id) {
        Piece piece = find(id);
        if (repository.isInAnyProgram(id)) {
            throw new ConflictException(
                    "Piece " + id + " is part of a concert program; remove it from the program first");
        }
        repository.delete(piece);
    }

    @Transactional(readOnly = true)
    public Piece find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Piece", id));
    }

    /**
     * Loads a piece that rehearsals and concerts of the given ensemble may use. A missing piece and a piece
     * from another ensemble are the same broken rule for the caller, so both answer 422.
     */
    @Transactional(readOnly = true)
    public Piece findInRepertoire(Long ensembleId, Long pieceId) {
        return repository.findById(pieceId)
                .filter(piece -> piece.belongsTo(ensembleId))
                .orElseThrow(() -> new BusinessRuleException(
                        "Piece " + pieceId + " is not in the repertoire of ensemble " + ensembleId));
    }
}
