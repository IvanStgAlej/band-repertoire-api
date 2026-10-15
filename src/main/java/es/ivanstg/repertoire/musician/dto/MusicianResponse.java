package es.ivanstg.repertoire.musician.dto;

import es.ivanstg.repertoire.musician.Musician;

public record MusicianResponse(Long id, String name, String email, String mainInstrument) {

    public static MusicianResponse from(Musician musician) {
        return new MusicianResponse(musician.getId(), musician.getName(), musician.getEmail(),
                musician.getMainInstrument());
    }
}
