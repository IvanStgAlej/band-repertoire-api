package es.ivanstg.repertoire.ensemble;

import java.time.LocalDate;

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

/**
 * A musician's place in an ensemble. It is an entity rather than a plain many-to-many because it carries its
 * own data: the instrument played in this ensemble and the date the musician joined.
 */
@Entity
@Table(name = "membership")
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ensemble_id")
    private Ensemble ensemble;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "musician_id")
    private Musician musician;

    @Column(nullable = false, length = 60)
    private String instrument;

    @Column(name = "joined_on", nullable = false)
    private LocalDate joinedOn;

    protected Membership() {
    }

    public Membership(Ensemble ensemble, Musician musician, String instrument, LocalDate joinedOn) {
        this.ensemble = ensemble;
        this.musician = musician;
        this.instrument = instrument.strip();
        this.joinedOn = joinedOn;
    }

    public Long getId() {
        return id;
    }

    public Ensemble getEnsemble() {
        return ensemble;
    }

    public Musician getMusician() {
        return musician;
    }

    public String getInstrument() {
        return instrument;
    }

    public LocalDate getJoinedOn() {
        return joinedOn;
    }
}
