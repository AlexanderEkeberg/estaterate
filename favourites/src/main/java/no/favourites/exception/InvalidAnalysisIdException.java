package no.favourites.exception;

public class InvalidAnalysisIdException extends RuntimeException {

    public InvalidAnalysisIdException() {
        super("analysisId must be a UUID");
    }
}
