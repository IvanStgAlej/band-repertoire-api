package es.ivanstg.repertoire.piece;

import java.net.URI;
import java.util.List;

import es.ivanstg.repertoire.common.PageResponse;
import es.ivanstg.repertoire.common.Sorting;
import es.ivanstg.repertoire.piece.dto.PieceRequest;
import es.ivanstg.repertoire.piece.dto.PieceResponse;
import es.ivanstg.repertoire.piece.dto.PieceSearch;
import es.ivanstg.repertoire.piece.dto.StalePieceResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Repertoire", description = "The pieces each ensemble plays")
public class PieceController {

    private final PieceService service;
    private final StalePieceService stalePieceService;

    public PieceController(PieceService service, StalePieceService stalePieceService) {
        this.service = service;
        this.stalePieceService = stalePieceService;
    }

    @GetMapping("/ensembles/{ensembleId}/pieces/stale")
    @Operation(summary = "Forgotten repertoire",
            description = "Pieces not rehearsed in the last N weeks (1-52, default 4). Pieces never rehearsed "
                    + "come first, then the longest forgotten. Future rehearsals do not count.")
    public List<StalePieceResponse> stale(@PathVariable Long ensembleId,
                                          @RequestParam(defaultValue = "4") @Min(1) @Max(52) int weeks) {
        return stalePieceService.findStale(ensembleId, weeks);
    }

    @PostMapping("/ensembles/{ensembleId}/pieces")
    public ResponseEntity<PieceResponse> create(@PathVariable Long ensembleId,
                                                @Valid @RequestBody PieceRequest request) {
        PieceResponse created = service.create(ensembleId, request);
        return ResponseEntity.created(URI.create("/api/pieces/" + created.id())).body(created);
    }

    @GetMapping("/ensembles/{ensembleId}/pieces")
    public PageResponse<PieceResponse> listByEnsemble(@PathVariable Long ensembleId,
                                                      @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return service.listByEnsemble(ensembleId, sortable(pageable));
    }

    @GetMapping("/pieces")
    @Operation(summary = "Search the repertoire",
            description = "All filters are optional. q matches the title or the composer, ignoring case.")
    public PageResponse<PieceResponse> search(@RequestParam(required = false) Long ensembleId,
                                              @RequestParam(required = false) @Min(1) @Max(5) Integer maxDifficulty,
                                              @RequestParam(required = false) String genre,
                                              @RequestParam(required = false) String q,
                                              @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return service.search(new PieceSearch(ensembleId, maxDifficulty, genre, q), sortable(pageable));
    }

    private static Pageable sortable(Pageable pageable) {
        return Sorting.requireSortable(pageable,
                "id", "title", "composer", "arranger", "durationSeconds", "difficulty", "genre");
    }

    @GetMapping("/pieces/{id}")
    public PieceResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/pieces/{id}")
    public PieceResponse update(@PathVariable Long id, @Valid @RequestBody PieceRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/pieces/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
