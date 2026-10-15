package es.ivanstg.repertoire.rehearsal;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import es.ivanstg.repertoire.common.BadRequestException;
import es.ivanstg.repertoire.common.BusinessRuleException;
import es.ivanstg.repertoire.common.NotFoundException;
import es.ivanstg.repertoire.common.PageResponse;
import es.ivanstg.repertoire.ensemble.Ensemble;
import es.ivanstg.repertoire.ensemble.EnsembleService;
import es.ivanstg.repertoire.ensemble.MembershipRepository;
import es.ivanstg.repertoire.musician.MusicianService;
import es.ivanstg.repertoire.piece.Piece;
import es.ivanstg.repertoire.piece.PieceService;
import es.ivanstg.repertoire.rehearsal.dto.AttendanceEntry;
import es.ivanstg.repertoire.rehearsal.dto.AttendanceRequest;
import es.ivanstg.repertoire.rehearsal.dto.RehearsalRequest;
import es.ivanstg.repertoire.rehearsal.dto.RehearsalResponse;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Responses are built inside the transaction: open-in-view is off, so lazy collections must be read here.
@Service
@Transactional
public class RehearsalService {

    private final RehearsalRepository repository;
    private final EnsembleService ensembleService;
    private final PieceService pieceService;
    private final MusicianService musicianService;
    private final MembershipRepository membershipRepository;

    public RehearsalService(RehearsalRepository repository, EnsembleService ensembleService,
                            PieceService pieceService, MusicianService musicianService,
                            MembershipRepository membershipRepository) {
        this.repository = repository;
        this.ensembleService = ensembleService;
        this.pieceService = pieceService;
        this.musicianService = musicianService;
        this.membershipRepository = membershipRepository;
    }

    public RehearsalResponse create(Long ensembleId, RehearsalRequest request) {
        Ensemble ensemble = ensembleService.find(ensembleId);
        Set<Piece> pieces = request.pieceIds().stream()
                .map(pieceId -> pieceService.findInRepertoire(ensembleId, pieceId))
                .collect(Collectors.toSet());
        Rehearsal rehearsal = new Rehearsal(ensemble, request.startsAt(), request.location(), request.notes(),
                pieces);
        return RehearsalResponse.from(repository.save(rehearsal));
    }

    @Transactional(readOnly = true)
    public RehearsalResponse get(Long id) {
        return RehearsalResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<RehearsalResponse> list(Long ensembleId, Pageable pageable) {
        ensembleService.find(ensembleId);
        return PageResponse.from(repository.findByEnsembleId(ensembleId, pageable).map(RehearsalResponse::from));
    }

    public RehearsalResponse recordAttendance(Long rehearsalId, AttendanceRequest request) {
        Rehearsal rehearsal = find(rehearsalId);
        Long ensembleId = rehearsal.getEnsemble().getId();
        Set<Long> seen = new HashSet<>();
        for (AttendanceEntry entry : request.entries()) {
            if (!seen.add(entry.musicianId())) {
                throw new BadRequestException("Musician " + entry.musicianId() + " appears more than once");
            }
        }
        for (AttendanceEntry entry : request.entries()) {
            if (!membershipRepository.existsByEnsembleIdAndMusicianId(ensembleId, entry.musicianId())) {
                throw new BusinessRuleException(
                        "Musician " + entry.musicianId() + " is not a member of ensemble " + ensembleId);
            }
            rehearsal.recordAttendance(musicianService.find(entry.musicianId()), entry.attended(), entry.reason());
        }
        return RehearsalResponse.from(rehearsal);
    }

    private Rehearsal find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Rehearsal", id));
    }
}
