package es.ivanstg.repertoire.rehearsal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import es.ivanstg.repertoire.ensemble.Ensemble;
import es.ivanstg.repertoire.musician.Musician;
import es.ivanstg.repertoire.piece.Piece;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/** A rehearsal of an ensemble: when and where, which pieces were worked on and who came. */
@Entity
@Table(name = "rehearsal")
public class Rehearsal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ensemble_id")
    private Ensemble ensemble;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(length = 160)
    private String location;

    @Column(length = 1000)
    private String notes;

    @ManyToMany
    @JoinTable(name = "rehearsal_piece",
            joinColumns = @JoinColumn(name = "rehearsal_id"),
            inverseJoinColumns = @JoinColumn(name = "piece_id"))
    private Set<Piece> pieces = new HashSet<>();

    @OneToMany(mappedBy = "rehearsal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attendance> attendance = new ArrayList<>();

    protected Rehearsal() {
    }

    public Rehearsal(Ensemble ensemble, LocalDateTime startsAt, String location, String notes, Set<Piece> pieces) {
        this.ensemble = ensemble;
        this.startsAt = startsAt;
        this.location = location;
        this.notes = notes;
        this.pieces.addAll(pieces);
    }

    /** Creates the musician's attendance record, or updates it if one already exists. */
    public void recordAttendance(Musician musician, boolean attended, String reason) {
        attendance.stream()
                .filter(record -> record.getMusician().getId().equals(musician.getId()))
                .findFirst()
                .ifPresentOrElse(
                        record -> record.update(attended, reason),
                        () -> attendance.add(new Attendance(this, musician, attended, reason)));
    }

    public Long getId() {
        return id;
    }

    public Ensemble getEnsemble() {
        return ensemble;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public String getLocation() {
        return location;
    }

    public String getNotes() {
        return notes;
    }

    public Set<Piece> getPieces() {
        return pieces;
    }

    public List<Attendance> getAttendance() {
        return attendance;
    }
}
