package es.ivanstg.repertoire.ensemble;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A band, big band, orchestra or combo. Its pieces, rehearsals and concerts are deleted with it. */
@Entity
@Table(name = "ensemble")
public class Ensemble {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnsembleType type;

    @Column(length = 120)
    private String city;

    protected Ensemble() {
    }

    public Ensemble(String name, EnsembleType type, String city) {
        update(name, type, city);
    }

    public void update(String name, EnsembleType type, String city) {
        this.name = name.strip();
        this.type = type;
        this.city = city;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public EnsembleType getType() {
        return type;
    }

    public String getCity() {
        return city;
    }
}
