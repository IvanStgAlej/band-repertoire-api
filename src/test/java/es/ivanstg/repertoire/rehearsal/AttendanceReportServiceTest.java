package es.ivanstg.repertoire.rehearsal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import es.ivanstg.repertoire.common.BadRequestException;
import es.ivanstg.repertoire.ensemble.Ensemble;
import es.ivanstg.repertoire.ensemble.EnsembleService;
import es.ivanstg.repertoire.ensemble.EnsembleType;
import es.ivanstg.repertoire.ensemble.Membership;
import es.ivanstg.repertoire.ensemble.MembershipRepository;
import es.ivanstg.repertoire.musician.Musician;
import es.ivanstg.repertoire.rehearsal.dto.AttendanceRateResponse;
import es.ivanstg.repertoire.rehearsal.dto.AttendedCount;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AttendanceReportServiceTest {

    private static final ZoneId ZONE = ZoneId.of("Europe/Madrid");
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 15);
    private static final LocalDate FROM = LocalDate.of(2026, 9, 1);
    private static final LocalDate TO = LocalDate.of(2026, 9, 30);

    @Mock
    private EnsembleService ensembleService;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private RehearsalRepository rehearsalRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    private AttendanceReportService service;
    private Ensemble band;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(ZonedDateTime.of(TODAY.atTime(12, 0), ZONE).toInstant(), ZONE);
        service = new AttendanceReportService(ensembleService, membershipRepository, rehearsalRepository,
                attendanceRepository, clock);
        band = new Ensemble("Riverside Concert Band", EnsembleType.BAND, null);
        ReflectionTestUtils.setField(band, "id", 1L);
    }

    @Test
    void ratesAreSortedLowestFirst() {
        givenMembers(member(1L, "Ana"), member(2L, "Ben"), member(3L, "Cleo"));
        when(rehearsalRepository.countByEnsembleIdAndStartsAtGreaterThanEqualAndStartsAtLessThan(
                1L, FROM.atStartOfDay(), TO.plusDays(1).atStartOfDay())).thenReturn(4L);
        when(attendanceRepository.countAttended(1L, FROM.atStartOfDay(), TO.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(new AttendedCount(1L, 4L), new AttendedCount(2L, 1L)));

        List<AttendanceRateResponse> report = service.report(1L, FROM, TO);

        assertThat(report)
                .extracting(AttendanceRateResponse::name, AttendanceRateResponse::rehearsals,
                        AttendanceRateResponse::attended, AttendanceRateResponse::attendanceRate)
                .containsExactly(
                        tuple("Cleo", 4L, 0L, 0.0),
                        tuple("Ben", 4L, 1L, 25.0),
                        tuple("Ana", 4L, 4L, 100.0));
    }

    @Test
    void ratesAreRoundedToOneDecimal() {
        givenMembers(member(1L, "Ana"));
        when(rehearsalRepository.countByEnsembleIdAndStartsAtGreaterThanEqualAndStartsAtLessThan(
                anyLong(), any(), any())).thenReturn(3L);
        when(attendanceRepository.countAttended(anyLong(), any(), any()))
                .thenReturn(List.of(new AttendedCount(1L, 2L)));

        assertThat(service.report(1L, FROM, TO).getFirst().attendanceRate()).isEqualTo(66.7);
    }

    @Test
    void noRehearsalsInThePeriodGivesNullRatesSortedByName() {
        givenMembers(member(2L, "Ben"), member(1L, "Ana"));
        when(rehearsalRepository.countByEnsembleIdAndStartsAtGreaterThanEqualAndStartsAtLessThan(
                anyLong(), any(), any())).thenReturn(0L);
        when(attendanceRepository.countAttended(anyLong(), any(), any())).thenReturn(List.of());

        assertThat(service.report(1L, FROM, TO))
                .extracting(AttendanceRateResponse::name, AttendanceRateResponse::attendanceRate)
                .containsExactly(tuple("Ana", null), tuple("Ben", null));
    }

    @Test
    void defaultsToTheLastNinetyDays() {
        givenMembers();
        when(rehearsalRepository.countByEnsembleIdAndStartsAtGreaterThanEqualAndStartsAtLessThan(
                anyLong(), any(), any())).thenReturn(0L);

        service.report(1L, null, null);

        verify(rehearsalRepository).countByEnsembleIdAndStartsAtGreaterThanEqualAndStartsAtLessThan(
                1L, TODAY.minusDays(90).atStartOfDay(), TODAY.plusDays(1).atStartOfDay());
    }

    @Test
    void fromAfterToIs400() {
        assertThatThrownBy(() -> service.report(1L, TO, FROM))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("'from' must not be after 'to'");
    }

    private void givenMembers(Membership... members) {
        when(ensembleService.find(1L)).thenReturn(band);
        when(membershipRepository.findByEnsembleIdOrderByMusicianNameAsc(1L)).thenReturn(List.of(members));
    }

    private Membership member(Long musicianId, String name) {
        Musician musician = new Musician(name, name.toLowerCase() + "@example.com", "Trumpet");
        ReflectionTestUtils.setField(musician, "id", musicianId);
        return new Membership(band, musician, "Trumpet", LocalDate.of(2020, 1, 1));
    }
}
