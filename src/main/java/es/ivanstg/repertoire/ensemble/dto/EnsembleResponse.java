package es.ivanstg.repertoire.ensemble.dto;

import es.ivanstg.repertoire.ensemble.Ensemble;
import es.ivanstg.repertoire.ensemble.EnsembleType;

public record EnsembleResponse(Long id, String name, EnsembleType type, String city) {

    public static EnsembleResponse from(Ensemble ensemble) {
        return new EnsembleResponse(ensemble.getId(), ensemble.getName(), ensemble.getType(), ensemble.getCity());
    }
}
