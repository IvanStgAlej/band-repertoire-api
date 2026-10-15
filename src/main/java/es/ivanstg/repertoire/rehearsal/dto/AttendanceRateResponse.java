package es.ivanstg.repertoire.rehearsal.dto;

/** {@code attendanceRate} is a percentage with one decimal, or null when there were no rehearsals. */
public record AttendanceRateResponse(Long musicianId, String name, String instrument, long rehearsals,
                                     long attended, Double attendanceRate) {
}
