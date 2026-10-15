package es.ivanstg.repertoire.ensemble;

import es.ivanstg.repertoire.common.NotFoundException;
import es.ivanstg.repertoire.common.PageResponse;
import es.ivanstg.repertoire.ensemble.dto.EnsembleRequest;
import es.ivanstg.repertoire.ensemble.dto.EnsembleResponse;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EnsembleService {

    private final EnsembleRepository repository;

    public EnsembleService(EnsembleRepository repository) {
        this.repository = repository;
    }

    public EnsembleResponse create(EnsembleRequest request) {
        Ensemble ensemble = new Ensemble(request.name(), request.type(), request.city());
        return EnsembleResponse.from(repository.save(ensemble));
    }

    @Transactional(readOnly = true)
    public EnsembleResponse get(Long id) {
        return EnsembleResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<EnsembleResponse> list(Pageable pageable) {
        return PageResponse.from(repository.findAll(pageable).map(EnsembleResponse::from));
    }

    public EnsembleResponse update(Long id, EnsembleRequest request) {
        Ensemble ensemble = find(id);
        ensemble.update(request.name(), request.type(), request.city());
        return EnsembleResponse.from(ensemble);
    }

    public void delete(Long id) {
        repository.delete(find(id));
    }

    @Transactional(readOnly = true)
    public Ensemble find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Ensemble", id));
    }
}
