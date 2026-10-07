package no.property.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidFinnUrlException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUrl(InvalidFinnUrlException e) {
        log.warn("Rejected URL: {}", e.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse("Invalid FINN URL"));
    }

    @ExceptionHandler(ScrapingFailedException.class)
    public ResponseEntity<ErrorResponse> handleScrapingFailed(ScrapingFailedException e) {
        log.error("Scraping failed", e);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse("Could not read the listing"));
    }
}
