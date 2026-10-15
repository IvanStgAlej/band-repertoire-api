package es.ivanstg.repertoire.rehearsal;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import es.ivanstg.repertoire.common.BadRequestException;
import es.ivanstg.repertoire.ensemble.EnsembleService;
import es.ivanstg.repertoire.ensemble.Membership;
import es.ivanstg.repertoire.ensemble.MembershipRepository;
import es.ivanstg.repertoire.rehearsal.dto.AttendanceRateResponse;
import es.ivanstg.repertoire.rehearsal.dto.AttendedCount;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Attendance rate of each current member over a period: rehearsals attended divided by the rehearsals the
 * member could have attended. Those are the ensemble's rehearsals in the period that have already happened
 * and took place after the member joined. A missing attendance record counts as absent.
 */
@Service
@Transactional(readOnly = true)
public class AttendanceReportService {

    static final int DEFAULT_PERIOD_DAYS = 90;

    private static final Comparator<AttendanceRateResponse> LOWEST_RATE_FIRST = Comparator
            .comparing(AttendanceRateResponse::attendanceRate, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(AttendanceRateResponse::name);

    private final EnsembleService ensembleService;
    private final MembershipRepository membershipRepository;
    private final RehearsalRepository rehearsalRepository;
    private final AttendanceRepository attendanceRepository;
    private final Clock clock;

    public AttendanceReportService(EnsembleService ensembleService, MembershipRepository membershipRepository,
                                   RehearsalRepository rehearsalRepository, AttendanceRepository attendanceRepository,
                                   Clock clock) {
        this.ensembleService = ensembleService;
        this.membershipRepository = membershipRepository;
        this.rehearsalRepository = rehearsalRepository;
        this.attendanceRepository = attendanceRepository;
        this.clock = clock;
    }

    /** {@code from} and {@code to} are inclusive dates; they default to the last 90 days up to today. */
    public List<AttendanceRateResponse> report(Long ensembleId, LocalDate from, LocalDate to) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate end = to != null ? to : now.toLocalDate();
        LocalDate start = from != null ? from : end.minusDays(DEFAULT_PERIOD_DAYS);
        if (start.isAfter(end)) {
            throw new BadRequestException("'from' must not be after 'to'");
        }
        ensembleService.find(ensembleId);
        LocalDateTime periodStart = start.atStartOfDay();
        LocalDateTime endOfPeriod = end.plusDays(1).atStartOfDay();
        // A rehearsal that has not happened yet cannot have been missed.
        LocalDateTime periodEnd = endOfPeriod.isAfter(now) ? now : endOfPeriod;

        List<LocalDateTime> rehearsals = rehearsalRepository.findStartTimes(ensembleId, periodStart, periodEnd);
        Map<Long, Long> attendedByMusician = attendanceRepository.countAttended(ensembleId, periodStart, periodEnd)
                .stream()
                .collect(Collectors.toMap(AttendedCount::musicianId, AttendedCount::attended));

        return membershipRepository.findByEnsembleIdOrderByMusicianNameAsc(ensembleId).stream()
                .map(member -> rate(member, rehearsals, attendedByMusician))
                .sorted(LOWEST_RATE_FIRST)
                .toList();
    }

    private static AttendanceRateResponse rate(Membership member, List<LocalDateTime> rehearsals,
                                               Map<Long, Long> attendedByMusician) {
        LocalDateTime joined = member.getJoinedOn().atStartOfDay();
        long possible = rehearsals.stream().filter(startsAt -> !startsAt.isBefore(joined)).count();
        Long musicianId = member.getMusician().getId();
        long attended = Math.min(attendedByMusician.getOrDefault(musicianId, 0L), possible);
        Double rate = possible == 0 ? null : Math.round(attended * 1000.0 / possible) / 10.0;
        return new AttendanceRateResponse(musicianId, member.getMusician().getName(), member.getInstrument(),
                possible, attended, rate);
    }
}
