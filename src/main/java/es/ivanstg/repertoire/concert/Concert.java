package es.ivanstg.repertoire.concert;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import es.ivanstg.repertoire.ensemble.Ensemble;
import es.ivanstg.repertoire.piece.Piece;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/** A concert and its program, the ordered list of pieces to perform. */
@Entity
@Table(name = "concert")
public class Concert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ensemble_id")
    private Ensemble ensemble;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(name = "concert_date", nullable = false)
    private LocalDate date;

    @Column(length = 160)
    private String venue;

    @OneToMany(mappedBy = "concert", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder")
    private List<ProgramItem> program = new ArrayList<>();

    protected Concert() {
    }

    public Concert(Ensemble ensemble, String title, LocalDate date, String venue) {
        this.ensemble = ensemble;
        this.title = title.strip();
        this.date = date;
        this.venue = venue;
    }

    void clearProgram() {
        program.clear();
    }

    /** Appends the pieces in order, numbering them from 1. Call {@link #clearProgram()} (and flush) first. */
    void addToProgram(List<Piece> pieces) {
        for (Piece piece : pieces) {
            program.add(new ProgramItem(this, piece, program.size() + 1));
        }
    }

    public Long getId() {
        return id;
    }

    public Ensemble getEnsemble() {
        return ensemble;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getVenue() {
        return venue;
    }

    public List<ProgramItem> getProgram() {
        return program;
    }
}
