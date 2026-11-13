package no.favourites.exception;

public class AggregatorUnavailableException extends RuntimeException {

    public AggregatorUnavailableException(Throwable cause) {
        super("Analysis service is unavailable", cause);
    }
}
