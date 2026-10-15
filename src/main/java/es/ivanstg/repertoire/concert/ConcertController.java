package es.ivanstg.repertoire.concert;

import java.net.URI;

import es.ivanstg.repertoire.common.PageResponse;
import es.ivanstg.repertoire.concert.dto.ConcertRequest;
import es.ivanstg.repertoire.concert.dto.ConcertResponse;
import es.ivanstg.repertoire.concert.dto.ProgramRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Concerts", description = "Concerts and their ordered programs")
public class ConcertController {

    private final ConcertService service;

    public ConcertController(ConcertService service) {
        this.service = service;
    }

    @PostMapping("/ensembles/{ensembleId}/concerts")
    public ResponseEntity<ConcertResponse> create(@PathVariable Long ensembleId,
                                                  @Valid @RequestBody ConcertRequest request) {
        ConcertResponse created = service.create(ensembleId, request);
        return ResponseEntity.created(URI.create("/api/concerts/" + created.id())).body(created);
    }

    @GetMapping("/ensembles/{ensembleId}/concerts")
    public PageResponse<ConcertResponse> list(@PathVariable Long ensembleId,
                                              @PageableDefault(size = 20, sort = "date") Pageable pageable) {
        return service.list(ensembleId, pageable);
    }

    @GetMapping("/concerts/{id}")
    public ConcertResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/concerts/{id}/program")
    @Operation(summary = "Replace the program",
            description = "Pieces are performed in list order. Repeating a piece is 409; a piece outside the "
                    + "ensemble's repertoire is 422. An empty list clears the program.")
    public ConcertResponse replaceProgram(@PathVariable Long id, @Valid @RequestBody ProgramRequest request) {
        return service.replaceProgram(id, request);
    }
}
