package es.ivanstg.repertoire.rehearsal.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** {@code pieceIds} lists the pieces worked on; it may be omitted. */
public record RehearsalRequest(
        @NotNull LocalDateTime startsAt,
        @Size(max = 160) String location,
        @Size(max = 1000) String notes,
        List<@NotNull Long> pieceIds) {

    public RehearsalRequest {
        pieceIds = pieceIds == null ? List.of() : List.copyOf(pieceIds);
    }
}
