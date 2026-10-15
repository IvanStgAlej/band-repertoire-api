package es.ivanstg.repertoire.common;

/** The request is well formed but breaks a domain rule. Mapped to 422 Unprocessable Content. */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String detail) {
        super(detail);
    }
}
