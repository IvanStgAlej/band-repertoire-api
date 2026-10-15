package es.ivanstg.repertoire.rehearsal.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

/** Attendance to record. Musicians not listed keep their current record. */
public record AttendanceRequest(@NotEmpty List<@Valid AttendanceEntry> entries) {
}
