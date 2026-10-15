package es.ivanstg.repertoire.rehearsal.dto;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import es.ivanstg.repertoire.rehearsal.Rehearsal;

public record RehearsalResponse(Long id, Long ensembleId, LocalDateTime startsAt, String location, String notes,
                                List<PieceRef> pieces, List<AttendanceResponse> attendance) {

    public static RehearsalResponse from(Rehearsal rehearsal) {
        List<PieceRef> pieces = rehearsal.getPieces().stream()
                .map(piece -> new PieceRef(piece.getId(), piece.getTitle()))
                .sorted(Comparator.comparing(PieceRef::title))
                .toList();
        List<AttendanceResponse> attendance = rehearsal.getAttendance().stream()
                .map(AttendanceResponse::from)
                .sorted(Comparator.comparing(AttendanceResponse::name))
                .toList();
        return new RehearsalResponse(rehearsal.getId(), rehearsal.getEnsemble().getId(), rehearsal.getStartsAt(),
                rehearsal.getLocation(), rehearsal.getNotes(), pieces, attendance);
    }
}
