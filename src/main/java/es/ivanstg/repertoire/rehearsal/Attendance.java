package es.ivanstg.repertoire.rehearsal;

import es.ivanstg.repertoire.musician.Musician;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Whether a musician came to a rehearsal. There is at most one record per rehearsal and musician. */
@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rehearsal_id")
    private Rehearsal rehearsal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "musician_id")
    private Musician musician;

    @Column(nullable = false)
    private boolean attended;

    @Column(length = 200)
    private String reason;

    protected Attendance() {
    }

    Attendance(Rehearsal rehearsal, Musician musician, boolean attended, String reason) {
        this.rehearsal = rehearsal;
        this.musician = musician;
        update(attended, reason);
    }

    void update(boolean attended, String reason) {
        this.attended = attended;
        this.reason = reason;
    }

    public Long getId() {
        return id;
    }

    public Rehearsal getRehearsal() {
        return rehearsal;
    }

    public Musician getMusician() {
        return musician;
    }

    public boolean isAttended() {
        return attended;
    }

    public String getReason() {
        return reason;
    }
}
