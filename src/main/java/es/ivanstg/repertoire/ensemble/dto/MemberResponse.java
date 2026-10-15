package es.ivanstg.repertoire.ensemble.dto;

import java.time.LocalDate;

import es.ivanstg.repertoire.ensemble.Membership;

public record MemberResponse(Long musicianId, String name, String email, String instrument, LocalDate joinedOn) {

    public static MemberResponse from(Membership membership) {
        var musician = membership.getMusician();
        return new MemberResponse(musician.getId(), musician.getName(), musician.getEmail(),
                membership.getInstrument(), membership.getJoinedOn());
    }
}
