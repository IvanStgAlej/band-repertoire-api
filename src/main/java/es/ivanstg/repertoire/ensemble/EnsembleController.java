package es.ivanstg.repertoire.ensemble;

import java.net.URI;

import es.ivanstg.repertoire.common.PageResponse;
import es.ivanstg.repertoire.common.Sorting;
import es.ivanstg.repertoire.ensemble.dto.EnsembleRequest;
import es.ivanstg.repertoire.ensemble.dto.EnsembleResponse;

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
@RequestMapping("/api/ensembles")
@Tag(name = "Ensembles", description = "Bands, big bands, orchestras and combos")
public class EnsembleController {

    private final EnsembleService service;

    public EnsembleController(EnsembleService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EnsembleResponse> create(@Valid @RequestBody EnsembleRequest request) {
        EnsembleResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/ensembles/" + created.id())).body(created);
    }

    @GetMapping
    public PageResponse<EnsembleResponse> list(@PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return service.list(Sorting.requireSortable(pageable, "id", "name", "type", "city"));
    }

    @GetMapping("/{id}")
    public EnsembleResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    public EnsembleResponse update(@PathVariable Long id, @Valid @RequestBody EnsembleRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
