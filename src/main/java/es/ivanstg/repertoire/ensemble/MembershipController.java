package es.ivanstg.repertoire.ensemble;

import java.util.List;

import es.ivanstg.repertoire.ensemble.dto.MemberResponse;
import es.ivanstg.repertoire.ensemble.dto.MembershipRequest;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ensembles/{ensembleId}/members")
@Tag(name = "Members", description = "Who plays in each ensemble, and on which instrument")
public class MembershipController {

    private final MembershipService service;

    public MembershipController(MembershipService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse add(@PathVariable Long ensembleId, @Valid @RequestBody MembershipRequest request) {
        return service.add(ensembleId, request);
    }

    @GetMapping
    public List<MemberResponse> list(@PathVariable Long ensembleId) {
        return service.list(ensembleId);
    }

    @DeleteMapping("/{musicianId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long ensembleId, @PathVariable Long musicianId) {
        service.remove(ensembleId, musicianId);
    }
}
