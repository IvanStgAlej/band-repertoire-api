package es.ivanstg.repertoire.musician;

import java.net.URI;

import es.ivanstg.repertoire.common.PageResponse;
import es.ivanstg.repertoire.common.Sorting;
import es.ivanstg.repertoire.musician.dto.MusicianRequest;
import es.ivanstg.repertoire.musician.dto.MusicianResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/musicians")
@Tag(name = "Musicians", description = "People who play in the ensembles")
public class MusicianController {

    private final MusicianService service;

    public MusicianController(MusicianService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<MusicianResponse> create(@Valid @RequestBody MusicianRequest request) {
        MusicianResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/musicians/" + created.id())).body(created);
    }

    @GetMapping
    public PageResponse<MusicianResponse> list(@PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return service.list(Sorting.requireSortable(pageable, "id", "name", "email", "mainInstrument"));
    }

    @GetMapping("/{id}")
    public MusicianResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    public MusicianResponse update(@PathVariable Long id, @Valid @RequestBody MusicianRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
