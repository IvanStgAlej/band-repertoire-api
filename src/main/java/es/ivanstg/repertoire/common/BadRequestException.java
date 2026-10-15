package es.ivanstg.repertoire.common;

/** Invalid request parameters that bean validation cannot express. Mapped to 400 Bad Request. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String detail) {
        super(detail);
    }
}
