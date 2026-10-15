package es.ivanstg.repertoire.concert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import es.ivanstg.repertoire.concert.dto.ConcertSummaryResponse;
import es.ivanstg.repertoire.concert.dto.PreparationWarning;
import es.ivanstg.repertoire.ensemble.Ensemble;
import es.ivanstg.repertoire.ensemble.EnsembleType;
import es.ivanstg.repertoire.piece.Piece;
import es.ivanstg.repertoire.piece.PieceRepository;
import es.ivanstg.repertoire.piece.dto.PieceLastRehearsal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ConcertSummaryServiceTest {

    private static final LocalDate CONCERT_DATE = LocalDate.of(2026, 11, 1);

    @Mock
    private ConcertService concertService;

    @Mock
    private PieceRepository pieceRepository;

    @InjectMocks
    private ConcertSummaryService service;

    private Ensemble band;
    private Concert concert;
    private Piece march;
    private Piece overture;
    private Piece suite;

    @BeforeEach
    void setUp() {
        band = withId(new Ensemble("Riverside Concert Band", EnsembleType.BAND, null), 1L);
        concert = withId(new Concert(band, "Autumn Concert", CONCERT_DATE, null), 10L);
        march = withId(new Piece(band, "March", null, null, 300, 2, null), 100L);
        overture = withId(new Piece(band, "Overture", null, null, 240, 4, null), 101L);
        suite = withId(new Piece(band, "Suite", null, null, 600, 3, null), 102L);
        when(concertService.find(10L)).thenReturn(concert);
    }

    @Test
    void computesTotalsAndAverageDifficulty() {
        concert.addToProgram(List.of(march, overture, suite));
        when(pieceRepository.findLastRehearsals(anyCollection(), any())).thenReturn(List.of(
                new PieceLastRehearsal(100L, "March", CONCERT_DATE.minusDays(2).atTime(19, 30)),
                new PieceLastRehearsal(101L, "Overture", CONCERT_DATE.minusDays(2).atTime(19, 30)),
                new PieceLastRehearsal(102L, "Suite", CONCERT_DATE.minusDays(2).atTime(19, 30))));

        ConcertSummaryResponse summary = service.summarise(10L);

        assertThat(summary.concertId()).isEqualTo(10L);
        assertThat(summary.pieceCount()).isEqualTo(3);
        assertThat(summary.totalDurationSeconds()).isEqualTo(1140);
        assertThat(summary.totalDuration()).isEqualTo("19:00");
        assertThat(summary.averageDifficulty()).isEqualTo(3.0);
        assertThat(summary.warnings()).isEmpty();
    }

    @Test
    void roundsTheAverageToOneDecimal() {
        concert.addToProgram(List.of(march, overture));
        Piece easy = withId(new Piece(band, "Easy", null, null, 60, 2, null), 103L);
        concert.addToProgram(List.of(easy));
        when(pieceRepository.findLastRehearsals(anyCollection(), any())).thenReturn(List.of());

        assertThat(service.summarise(10L).averageDifficulty()).isEqualTo(2.7);
    }

    @Test
    void warnsAboutPiecesNotRehearsedInTheThreeWeeksBefore() {
        concert.addToProgram(List.of(march, overture, suite));
        LocalDateTime tenDaysBefore = CONCERT_DATE.minusDays(10).atTime(19, 30);
        LocalDateTime thirtyDaysBefore = CONCERT_DATE.minusDays(30).atTime(19, 30);
        when(pieceRepository.findLastRehearsals(anyCollection(), any())).thenReturn(List.of(
                new PieceLastRehearsal(100L, "March", tenDaysBefore),
                new PieceLastRehearsal(101L, "Overture", thirtyDaysBefore),
                new PieceLastRehearsal(102L, "Suite", null)));

        List<PreparationWarning> warnings = service.summarise(10L).warnings();

        assertThat(warnings)
                .extracting(PreparationWarning::pieceId, PreparationWarning::lastRehearsedAt,
                        PreparationWarning::message)
                .containsExactly(
                        tuple(101L, thirtyDaysBefore,
                                "Last rehearsed on 2026-10-02, more than 3 weeks before the concert"),
                        tuple(102L, null, "Never rehearsed before the concert"));
        verify(pieceRepository).findLastRehearsals(List.of(100L, 101L, 102L), CONCERT_DATE.atStartOfDay());
    }

    @Test
    void emptyProgramGivesZeroTotalsAndDoesNotQuery() {
        ConcertSummaryResponse summary = service.summarise(10L);

        assertThat(summary.pieceCount()).isZero();
        assertThat(summary.totalDurationSeconds()).isZero();
        assertThat(summary.totalDuration()).isEqualTo("0:00");
        assertThat(summary.averageDifficulty()).isNull();
        assertThat(summary.warnings()).isEmpty();
        verify(pieceRepository, never()).findLastRehearsals(anyCollection(), any());
    }

    private static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}
