package no.property.validation;

import no.property.exception.InvalidFinnUrlException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FinnUrlValidatorTest {

    private final FinnUrlValidator validator = new FinnUrlValidator();

    @Test
    void acceptsValidListingUrl() {
        URI uri = validator.validate("https://www.finn.no/realestate/homes/ad.html?finnkode=473655500");
        assertEquals("www.finn.no", uri.getHost());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "   ",
            "not a url",
            "http://www.finn.no/realestate/homes/ad.html?finnkode=1",
            "ftp://www.finn.no/realestate/homes/ad.html",
            "https://evil.com/realestate/homes/ad.html",
            "https://www.finn.no.evil.com/realestate/homes/ad.html",
            "https://www.finn.no@evil.com/realestate/homes/ad.html",
            "https://www.finn.no:8080/realestate/homes/ad.html",
            "https://www.finn.no/realestate/homes/../../admin",
            "https://www.finn.no/job/fulltime/"
    })
    void rejectsInvalidUrls(String url) {
        assertThrows(InvalidFinnUrlException.class, () -> validator.validate(url));
    }
}
