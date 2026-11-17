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
        log.atWarn()
                .addKeyValue("event_type", "url_rejected")
                .addKeyValue("reason", e.getMessage())
                .log("Rejected URL");
        return ResponseEntity.badRequest().body(new ErrorResponse("Invalid FINN URL"));
    }

    @ExceptionHandler(ScrapingFailedException.class)
    public ResponseEntity<ErrorResponse> handleScrapingFailed(ScrapingFailedException e) {
        log.atError()
                .setCause(e)
                .addKeyValue("event_type", "scraping_failed")
                .log("Scraping failed");
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse("Could not read the listing"));
    }

    @ExceptionHandler(EventPublishFailedException.class)
    public ResponseEntity<ErrorResponse> handlePublishFailed(EventPublishFailedException e) {
        log.atError()
                .setCause(e)
                .addKeyValue("event_type", "property_publish_failed")
                .log("Event publish failed");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("Service temporarily unavailable"));
    }
}
