package es.ivanstg.repertoire.rehearsal;

import java.time.LocalDateTime;
import java.util.List;

import es.ivanstg.repertoire.rehearsal.dto.AttendedCount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    /** Rehearsals attended per musician, for the ensemble's rehearsals in [from, to). */
    @Query("""
            select new es.ivanstg.repertoire.rehearsal.dto.AttendedCount(a.musician.id, count(a))
            from Attendance a
            where a.rehearsal.ensemble.id = :ensembleId and a.attended = true
              and a.rehearsal.startsAt >= :from and a.rehearsal.startsAt < :to
            group by a.musician.id
            """)
    List<AttendedCount> countAttended(Long ensembleId, LocalDateTime from, LocalDateTime to);
}
