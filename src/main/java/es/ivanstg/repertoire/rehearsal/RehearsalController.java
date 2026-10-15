package es.ivanstg.repertoire.rehearsal;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import es.ivanstg.repertoire.common.PageResponse;
import es.ivanstg.repertoire.rehearsal.dto.AttendanceRateResponse;
import es.ivanstg.repertoire.rehearsal.dto.AttendanceRequest;
import es.ivanstg.repertoire.rehearsal.dto.RehearsalRequest;
import es.ivanstg.repertoire.rehearsal.dto.RehearsalResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Rehearsals", description = "Rehearsals, the pieces worked on and who attended")
public class RehearsalController {

    private final RehearsalService service;
    private final AttendanceReportService attendanceReportService;

    public RehearsalController(RehearsalService service, AttendanceReportService attendanceReportService) {
        this.service = service;
        this.attendanceReportService = attendanceReportService;
    }

    @GetMapping("/ensembles/{ensembleId}/attendance")
    @Operation(summary = "Attendance report",
            description = "Attendance rate of each member between from and to (inclusive ISO dates, default: "
                    + "the last 90 days), lowest first. The rate is null when there were no rehearsals.")
    public List<AttendanceRateResponse> attendanceReport(
            @PathVariable Long ensembleId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return attendanceReportService.report(ensembleId, from, to);
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
