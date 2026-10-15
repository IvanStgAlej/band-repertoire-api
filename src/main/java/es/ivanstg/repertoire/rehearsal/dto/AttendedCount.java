package es.ivanstg.repertoire.rehearsal.dto;

/** Query result: how many rehearsals a musician attended in a period. */
public record AttendedCount(Long musicianId, Long attended) {
}
