package no.property.exception;

public class ScrapingFailedException extends RuntimeException {
    public ScrapingFailedException(String message) {
        super(message);
    }

    public ScrapingFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
