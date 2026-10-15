package es.ivanstg.repertoire.common;

/** A requested resource does not exist. Mapped to 404 Not Found. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String resource, Long id) {
        super(resource + " " + id + " not found");
    }

    public NotFoundException(String detail) {
        super(detail);
    }
}
