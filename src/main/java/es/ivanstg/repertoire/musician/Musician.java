package es.ivanstg.repertoire.musician;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A person who plays in one or more ensembles. The email is unique and stored in lower case. */
@Entity
@Table(name = "musician")
public class Musician {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 180, unique = true)
    private String email;

    @Column(name = "main_instrument", nullable = false, length = 60)
    private String mainInstrument;

    protected Musician() {
    }

    public Musician(String name, String email, String mainInstrument) {
        update(name, email, mainInstrument);
    }

    public void update(String name, String email, String mainInstrument) {
        this.name = name.strip();
        this.email = normaliseEmail(email);
        this.mainInstrument = mainInstrument.strip();
    }

    public static String normaliseEmail(String email) {
        return email.strip().toLowerCase();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMainInstrument() {
        return mainInstrument;
    }
}
