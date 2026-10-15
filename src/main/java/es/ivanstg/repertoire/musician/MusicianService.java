package es.ivanstg.repertoire.musician;

import es.ivanstg.repertoire.common.ConflictException;
import es.ivanstg.repertoire.common.NotFoundException;
import es.ivanstg.repertoire.common.PageResponse;
import es.ivanstg.repertoire.musician.dto.MusicianRequest;
import es.ivanstg.repertoire.musician.dto.MusicianResponse;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MusicianService {

    private final MusicianRepository repository;

    public MusicianService(MusicianRepository repository) {
        this.repository = repository;
    }

    public MusicianResponse create(MusicianRequest request) {
        String email = Musician.normaliseEmail(request.email());
        if (repository.existsByEmailIgnoreCase(email)) {
            throw duplicateEmail(email);
        }
        Musician musician = new Musician(request.name(), email, request.mainInstrument());
        return MusicianResponse.from(repository.save(musician));
    }

    @Transactional(readOnly = true)
    public MusicianResponse get(Long id) {
        return MusicianResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<MusicianResponse> list(Pageable pageable) {
        return PageResponse.from(repository.findAll(pageable).map(MusicianResponse::from));
    }

    public MusicianResponse update(Long id, MusicianRequest request) {
        Musician musician = find(id);
        String email = Musician.normaliseEmail(request.email());
        if (repository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw duplicateEmail(email);
        }
        musician.update(request.name(), email, request.mainInstrument());
        return MusicianResponse.from(musician);
    }

    public void delete(Long id) {
        repository.delete(find(id));
    }

    @Transactional(readOnly = true)
    public Musician find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Musician", id));
    }

    private static ConflictException duplicateEmail(String email) {
        return new ConflictException("A musician with email " + email + " already exists");
    }
}
