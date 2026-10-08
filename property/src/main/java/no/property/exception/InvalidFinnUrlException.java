package no.property.exception;

public class InvalidFinnUrlException extends RuntimeException {
    public InvalidFinnUrlException(String message) {
        super(message);
    }
}
