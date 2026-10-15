package es.ivanstg.repertoire.concert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import es.ivanstg.repertoire.concert.dto.ConcertSummaryResponse;
import es.ivanstg.repertoire.concert.dto.PreparationWarning;
import es.ivanstg.repertoire.piece.Piece;
import es.ivanstg.repertoire.piece.PieceRepository;
import es.ivanstg.repertoire.piece.dto.PieceLastRehearsal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sums up a concert program and flags pieces that are not ready: a piece needs a rehearsal in the three weeks
 * before the concert date. Rehearsals scheduled before the concert count, even if they are still in the future.
 * A rehearsal on the concert day itself does not: a concert has a date but no time, so the cut-off is the start
 * of that day, and a warm-up or sound check on the day is not preparation.
 */
@Service
@Transactional(readOnly = true)
public class ConcertSummaryService {

    static final int PREPARATION_WEEKS = 3;

    private final ConcertService concertService;
    private final PieceRepository pieceRepository;

    public ConcertSummaryService(ConcertService concertService, PieceRepository pieceRepository) {
        this.concertService = concertService;
        this.pieceRepository = pieceRepository;
    }

    public ConcertSummaryResponse summarise(Long concertId) {
        Concert concert = concertService.find(concertId);
        List<Piece> pieces = concert.getProgram().stream().map(ProgramItem::getPiece).toList();
        int totalSeconds = pieces.stream().mapToInt(Piece::getDurationSeconds).sum();
        Double averageDifficulty = pieces.isEmpty() ? null
                : Math.round(pieces.stream().mapToInt(Piece::getDifficulty).average().orElseThrow() * 10) / 10.0;
        return new ConcertSummaryResponse(concert.getId(), concert.getTitle(), concert.getDate(), pieces.size(),
                totalSeconds, DurationFormat.format(totalSeconds), averageDifficulty, warnings(concert, pieces));
    }

    private List<PreparationWarning> warnings(Concert concert, List<Piece> pieces) {
        if (pieces.isEmpty()) {
            return List.of();
        }
        LocalDateTime concertStart = concert.getDate().atStartOfDay();
        LocalDateTime threshold = concertStart.minusWeeks(PREPARATION_WEEKS);
        Map<Long, LocalDateTime> lastRehearsals = pieceRepository
                .findLastRehearsals(pieces.stream().map(Piece::getId).toList(), concertStart).stream()
                .filter(row -> row.lastRehearsedAt() != null)
                .collect(Collectors.toMap(PieceLastRehearsal::pieceId, PieceLastRehearsal::lastRehearsedAt));

        List<PreparationWarning> warnings = new ArrayList<>();
        for (Piece piece : pieces) {
            LocalDateTime last = lastRehearsals.get(piece.getId());
            if (last == null) {
                warnings.add(new PreparationWarning(piece.getId(), piece.getTitle(), null,
                        "Never rehearsed before the concert"));
            } else if (last.isBefore(threshold)) {
                warnings.add(new PreparationWarning(piece.getId(), piece.getTitle(), last,
                        "Last rehearsed on %s, more than %d weeks before the concert"
                                .formatted(last.toLocalDate(), PREPARATION_WEEKS)));
            }
        }
        return warnings;
    }
}
