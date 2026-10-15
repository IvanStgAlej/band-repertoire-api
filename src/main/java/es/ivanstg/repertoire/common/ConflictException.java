package es.ivanstg.repertoire.common;

/** The request clashes with existing data (duplicates, resources in use). Mapped to 409 Conflict. */
public class ConflictException extends RuntimeException {

    public ConflictException(String detail) {
        super(detail);
    }
}
