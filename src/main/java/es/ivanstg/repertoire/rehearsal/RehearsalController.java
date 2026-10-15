package es.ivanstg.repertoire.rehearsal;

import java.net.URI;

import es.ivanstg.repertoire.common.PageResponse;
import es.ivanstg.repertoire.rehearsal.dto.AttendanceRequest;
import es.ivanstg.repertoire.rehearsal.dto.RehearsalRequest;
import es.ivanstg.repertoire.rehearsal.dto.RehearsalResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@Tag(name = "Rehearsals", description = "Rehearsals, the pieces worked on and who attended")
public class RehearsalController {

    private final RehearsalService service;

    public RehearsalController(RehearsalService service) {
        this.service = service;
    }

    @PostMapping("/ensembles/{ensembleId}/rehearsals")
    public ResponseEntity<RehearsalResponse> create(@PathVariable Long ensembleId,
                                                    @Valid @RequestBody RehearsalRequest request) {
        RehearsalResponse created = service.create(ensembleId, request);
        return ResponseEntity.created(URI.create("/api/rehearsals/" + created.id())).body(created);
    }

    @GetMapping("/ensembles/{ensembleId}/rehearsals")
    public PageResponse<RehearsalResponse> list(
            @PathVariable Long ensembleId,
            @PageableDefault(size = 20, sort = "startsAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.list(ensembleId, pageable);
    }

    @GetMapping("/rehearsals/{id}")
    public RehearsalResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/rehearsals/{id}/attendance")
    @Operation(summary = "Record attendance",
            description = "Creates or updates one record per listed musician. Only members can be recorded.")
    public RehearsalResponse recordAttendance(@PathVariable Long id, @Valid @RequestBody AttendanceRequest request) {
        return service.recordAttendance(id, request);
    }
}
