package es.ivanstg.repertoire.concert;

import es.ivanstg.repertoire.piece.Piece;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** One position in a concert program. A piece appears at most once per concert. */
@Entity
@Table(name = "program_item")
public class ProgramItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "concert_id")
    private Concert concert;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "piece_id")
    private Piece piece;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    protected ProgramItem() {
    }

    ProgramItem(Concert concert, Piece piece, int sortOrder) {
        this.concert = concert;
        this.piece = piece;
        this.sortOrder = sortOrder;
    }

    public Long getId() {
        return id;
    }

    public Concert getConcert() {
        return concert;
    }

    public Piece getPiece() {
        return piece;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
