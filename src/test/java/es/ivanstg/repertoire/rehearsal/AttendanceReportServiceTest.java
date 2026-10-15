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
import java.time.LocalDateTime;
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
    private static final LocalDateTime NOW = TODAY.atTime(12, 0);
    private static final LocalDate FROM = LocalDate.of(2026, 9, 1);
    private static final LocalDate TO = LocalDate.of(2026, 9, 30);
    private static final LocalDate LONG_AGO = LocalDate.of(2020, 1, 1);
    private static final List<LocalDateTime> FOUR_SEPTEMBER_REHEARSALS = List.of(
            LocalDateTime.of(2026, 9, 3, 19, 30), LocalDateTime.of(2026, 9, 10, 19, 30),
            LocalDateTime.of(2026, 9, 17, 19, 30), LocalDateTime.of(2026, 9, 24, 19, 30));

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
        Clock clock = Clock.fixed(ZonedDateTime.of(NOW, ZONE).toInstant(), ZONE);
        service = new AttendanceReportService(ensembleService, membershipRepository, rehearsalRepository,
                attendanceRepository, clock);
        band = new Ensemble("Riverside Concert Band", EnsembleType.BAND, null);
        ReflectionTestUtils.setField(band, "id", 1L);
    }

    @Test
    void ratesAreSortedLowestFirst() {
        givenMembers(member(1L, "Ana", LONG_AGO), member(2L, "Ben", LONG_AGO), member(3L, "Cleo", LONG_AGO));
        when(rehearsalRepository.findStartTimes(1L, FROM.atStartOfDay(), TO.plusDays(1).atStartOfDay()))
                .thenReturn(FOUR_SEPTEMBER_REHEARSALS);
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
        givenMembers(member(1L, "Ana", LONG_AGO));
        when(rehearsalRepository.findStartTimes(anyLong(), any(), any()))
                .thenReturn(FOUR_SEPTEMBER_REHEARSALS.subList(0, 3));
        when(attendanceRepository.countAttended(anyLong(), any(), any()))
                .thenReturn(List.of(new AttendedCount(1L, 2L)));

        assertThat(service.report(1L, FROM, TO).getFirst().attendanceRate()).isEqualTo(66.7);
    }

    @Test
    void membersOnlyCountRehearsalsAfterTheyJoined() {
        givenMembers(member(1L, "Ana", LONG_AGO), member(2L, "Newcomer", LocalDate.of(2026, 9, 15)));
        when(rehearsalRepository.findStartTimes(anyLong(), any(), any())).thenReturn(FOUR_SEPTEMBER_REHEARSALS);
        when(attendanceRepository.countAttended(anyLong(), any(), any()))
                .thenReturn(List.of(new AttendedCount(1L, 4L), new AttendedCount(2L, 2L)));

        assertThat(service.report(1L, FROM, TO))
                .extracting(AttendanceRateResponse::name, AttendanceRateResponse::rehearsals,
                        AttendanceRateResponse::attendanceRate)
                .containsExactly(tuple("Ana", 4L, 100.0), tuple("Newcomer", 2L, 100.0));
    }

    @Test
    void futureRehearsalsAreNotCountedAsMissed() {
        givenMembers();
        when(rehearsalRepository.findStartTimes(anyLong(), any(), any())).thenReturn(List.of());

        service.report(1L, FROM, TODAY.plusDays(30));

        verify(rehearsalRepository).findStartTimes(1L, FROM.atStartOfDay(), NOW);
        verify(attendanceRepository).countAttended(1L, FROM.atStartOfDay(), NOW);
    }

    @Test
    void noRehearsalsInThePeriodGivesNullRatesSortedByName() {
        givenMembers(member(2L, "Ben", LONG_AGO), member(1L, "Ana", LONG_AGO));
        when(rehearsalRepository.findStartTimes(anyLong(), any(), any())).thenReturn(List.of());
        when(attendanceRepository.countAttended(anyLong(), any(), any())).thenReturn(List.of());

        assertThat(service.report(1L, FROM, TO))
                .extracting(AttendanceRateResponse::name, AttendanceRateResponse::attendanceRate)
                .containsExactly(tuple("Ana", null), tuple("Ben", null));
    }

    @Test
    void defaultsToTheLastNinetyDaysUpToNow() {
        givenMembers();
        when(rehearsalRepository.findStartTimes(anyLong(), any(), any())).thenReturn(List.of());

        service.report(1L, null, null);

        verify(rehearsalRepository).findStartTimes(1L, TODAY.minusDays(90).atStartOfDay(), NOW);
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

    private Membership member(Long musicianId, String name, LocalDate joinedOn) {
        Musician musician = new Musician(name, name.toLowerCase() + "@example.com", "Trumpet");
        ReflectionTestUtils.setField(musician, "id", musicianId);
        return new Membership(band, musician, "Trumpet", joinedOn);
    }
}
