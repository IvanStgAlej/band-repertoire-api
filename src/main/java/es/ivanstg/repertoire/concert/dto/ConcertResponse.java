package es.ivanstg.repertoire.concert.dto;

import java.time.LocalDate;
import java.util.List;

import es.ivanstg.repertoire.concert.Concert;

public record ConcertResponse(Long id, Long ensembleId, String title, LocalDate date, String venue,
                              List<ProgramEntryResponse> program) {

    public static ConcertResponse from(Concert concert) {
        return new ConcertResponse(concert.getId(), concert.getEnsemble().getId(), concert.getTitle(),
                concert.getDate(), concert.getVenue(),
                concert.getProgram().stream().map(ProgramEntryResponse::from).toList());
    }
}
