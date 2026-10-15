package es.ivanstg.repertoire.piece;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import es.ivanstg.repertoire.ensemble.Ensemble;
import es.ivanstg.repertoire.ensemble.EnsembleType;
import es.ivanstg.repertoire.piece.dto.PieceLastRehearsal;
import es.ivanstg.repertoire.rehearsal.Rehearsal;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

/** Runs the custom JPQL queries against the real schema (Flyway on H2 in PostgreSQL mode). */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class PieceRepositoryTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 15, 12, 0);
    private static final LocalDateTime FOUR_WEEKS_AGO = NOW.minusWeeks(4);

    @Autowired
    private PieceRepository repository;

    @Autowired
    private EntityManager entityManager;

    private Ensemble band;

    @BeforeEach
    void createBand() {
        band = persist(new Ensemble("Riverside Concert Band", EnsembleType.BAND, null));
    }

    @Test
    void neverRehearsedPiecesComeFirstThenTheOldest() {
        Piece never = piece(band, "Never Rehearsed");
        Piece old = piece(band, "Rehearsed In August");
        Piece recent = piece(band, "Rehearsed Last Week");
        rehearsal(band, LocalDateTime.of(2026, 8, 1, 19, 30), old);
        rehearsal(band, LocalDateTime.of(2026, 10, 10, 19, 30), recent);

        List<PieceLastRehearsal> stale = repository.findStale(band.getId(), NOW, FOUR_WEEKS_AGO);

        assertThat(stale)
                .extracting(PieceLastRehearsal::pieceId, PieceLastRehearsal::lastRehearsedAt)
                .containsExactly(
                        tuple(never.getId(), null),
                        tuple(old.getId(), LocalDateTime.of(2026, 8, 1, 19, 30)));
    }

    @Test
    void aRecentRehearsalWinsOverAnOldOne() {
        Piece piece = piece(band, "Arsenal");
        rehearsal(band, LocalDateTime.of(2026, 6, 1, 19, 30), piece);
        rehearsal(band, LocalDateTime.of(2026, 10, 14, 19, 30), piece);

        assertThat(repository.findStale(band.getId(), NOW, FOUR_WEEKS_AGO)).isEmpty();
    }

    @Test
    void futureRehearsalsDoNotCount() {
        Piece piece = piece(band, "Arsenal");
        rehearsal(band, NOW.plusDays(1), piece);

        assertThat(repository.findStale(band.getId(), NOW, FOUR_WEEKS_AGO))
                .extracting(PieceLastRehearsal::pieceId, PieceLastRehearsal::lastRehearsedAt)
                .containsExactly(tuple(piece.getId(), null));
    }

    @Test
    void piecesFromOtherEnsemblesAreIgnored() {
        Ensemble bigBand = persist(new Ensemble("Night Owls Big Band", EnsembleType.BIG_BAND, null));
        piece(bigBand, "In the Mood");

        assertThat(repository.findStale(band.getId(), NOW, FOUR_WEEKS_AGO)).isEmpty();
    }

    @Test
    void lastRehearsalsOnlyLookBeforeTheCutOff() {
        Piece rehearsedBefore = piece(band, "Arsenal");
        Piece onlyAfter = piece(band, "El Camino Real");
        LocalDateTime concert = LocalDateTime.of(2026, 11, 1, 0, 0);
        rehearsal(band, LocalDateTime.of(2026, 10, 20, 19, 30), rehearsedBefore);
        rehearsal(band, LocalDateTime.of(2026, 10, 28, 19, 30), rehearsedBefore);
        rehearsal(band, LocalDateTime.of(2026, 11, 5, 19, 30), rehearsedBefore, onlyAfter);

        List<PieceLastRehearsal> result =
                repository.findLastRehearsals(List.of(rehearsedBefore.getId(), onlyAfter.getId()), concert);

        assertThat(result)
                .extracting(PieceLastRehearsal::pieceId, PieceLastRehearsal::lastRehearsedAt)
                .containsExactlyInAnyOrder(
                        tuple(rehearsedBefore.getId(), LocalDateTime.of(2026, 10, 28, 19, 30)),
                        tuple(onlyAfter.getId(), null));
    }

    private Piece piece(Ensemble ensemble, String title) {
        return persist(new Piece(ensemble, title, null, null, 300, 3, null));
    }

    private void rehearsal(Ensemble ensemble, LocalDateTime startsAt, Piece... pieces) {
        persist(new Rehearsal(ensemble, startsAt, null, null, Set.of(pieces)));
    }

    private <T> T persist(T entity) {
        entityManager.persist(entity);
        return entity;
    }
}
