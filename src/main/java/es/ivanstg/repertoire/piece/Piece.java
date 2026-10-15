package es.ivanstg.repertoire.piece;

import java.util.HashSet;
import java.util.Set;

import es.ivanstg.repertoire.ensemble.Ensemble;
import es.ivanstg.repertoire.rehearsal.Rehearsal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** A work in an ensemble's repertoire. */
@Entity
@Table(name = "piece")
public class Piece {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ensemble_id")
    private Ensemble ensemble;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 120)
    private String composer;

    @Column(length = 120)
    private String arranger;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @Column(nullable = false)
    private int difficulty;

    @Column(length = 60)
    private String genre;

    // Inverse side, only navigated by queries ("when was this piece last rehearsed?").
    @ManyToMany(mappedBy = "pieces")
    private Set<Rehearsal> rehearsals = new HashSet<>();

    protected Piece() {
    }

    public Piece(Ensemble ensemble, String title, String composer, String arranger, int durationSeconds,
                 int difficulty, String genre) {
        this.ensemble = ensemble;
        update(title, composer, arranger, durationSeconds, difficulty, genre);
    }

    public void update(String title, String composer, String arranger, int durationSeconds, int difficulty,
                       String genre) {
        this.title = title.strip();
        this.composer = composer;
        this.arranger = arranger;
        this.durationSeconds = durationSeconds;
        this.difficulty = difficulty;
        this.genre = genre;
    }

    public boolean belongsTo(Long ensembleId) {
        return ensemble.getId().equals(ensembleId);
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

    public String getComposer() {
        return composer;
    }

    public String getArranger() {
        return arranger;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public String getGenre() {
        return genre;
    }
}
