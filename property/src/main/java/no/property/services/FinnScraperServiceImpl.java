package no.property.services;

import lombok.extern.slf4j.Slf4j;
import no.property.client.FinnClient;
import no.property.dto.PropertyDetailsDTO;
import no.property.dto.PropertyLocationDTO;
import no.property.dto.ScrapeResponseDTO;
import no.property.event.PropertyEventPublisher;
import no.property.event.CoordinatesSource;
import no.property.event.PropertySubmittedEvent;
import no.property.exception.ScrapingFailedException;
import no.property.mock.MockPropertyData;
import no.property.validation.FinnUrlValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class FinnScraperServiceImpl implements FinnScraperService {

    static final String MOCK_MESSAGE = "We could not read the listing. The report shows sample data from Oslo.";
    static final String DEMO_MESSAGE = "FINN blocks automated access. The report uses saved data for this listing.";

    private final FinnClient finnClient;
    private final FinnHtmlParser finnAdHtmlParser;
    private final FinnUrlValidator finnUrlValidator;
    private final PropertyEventPublisher eventPublisher;
    private final MockPropertyData mock;
    private final boolean fallbackEnabled;
    private final boolean mockForced;

    public FinnScraperServiceImpl(
            FinnClient finnClient,
            FinnHtmlParser finnAdHtmlParser,
            FinnUrlValidator finnUrlValidator,
            PropertyEventPublisher eventPublisher,
            MockPropertyData mock,
            @Value("${property.mock.fallback-enabled:false}") boolean fallbackEnabled,
            @Value("${property.mock.force:false}") boolean mockForced) {
        this.finnClient = finnClient;
        this.finnAdHtmlParser = finnAdHtmlParser;
        this.finnUrlValidator = finnUrlValidator;
        this.eventPublisher = eventPublisher;
        this.mock = mock;
        this.fallbackEnabled = fallbackEnabled;
        this.mockForced = mockForced;
    }

    @Override
    public ScrapeResponseDTO scrape(String url) {
        // ugyldig lenke gir 400 og blir aldri mock
        URI uri = finnUrlValidator.validate(url);

        // hele boligen er enten fra annonsen eller fra mock, aldri en blanding
        CoordinatesSource source = CoordinatesSource.SCRAPED;
        String message = null;
        PropertyLocationDTO property = demoListingOrNull(uri);
        if (property != null) {
            source = CoordinatesSource.MOCK;
            message = DEMO_MESSAGE;
        } else {
            property = scrapeOrNull(uri);
            if (property == null) {
                source = CoordinatesSource.MOCK;
                message = MOCK_MESSAGE;
                property = mock.fallback(uri.toString());
            }
        }
        String analysisId = UUID.randomUUID().toString();

        eventPublisher.publish(new PropertySubmittedEvent(
                UUID.randomUUID(),
                "PropertySubmitted",
                1,
                Instant.now(),
                analysisId,
                property.getUrl(),
                property.getPostalCode(),
                property.getLatitude(),
                property.getLongitude(),
                source,
                property.getMunicipalityNumber(),
                property.getDistrict()));

        return new ScrapeResponseDTO(analysisId, source, message);
    }

    // demo-annonser er lest av manuelt og brukes uten å spørre FINN, som blokkerer automatisk henting
    private PropertyLocationDTO demoListingOrNull(URI uri) {
        if (!fallbackEnabled) {
            return null;
        }
        PropertyLocationDTO demo = mock.demoListing(uri).orElse(null);
        if (demo != null) {
            log.info("Using saved demo listing: {}", uri);
        }
        return demo;
    }

    // gir null hvis mock skal brukes
    private PropertyLocationDTO scrapeOrNull(URI uri) {
        if (mockForced) {
            log.info("Mock property forced, skipping FINN: {}", uri);
            return null;
        }
        try {
            String html = finnClient.fetchFinnHtml(uri);
            PropertyLocationDTO property = finnAdHtmlParser.parse(html, uri.toString());
            if (!property.hasRequiredFields()) {
                throw new ScrapingFailedException("Listing is missing required fields: " + uri);
            }
            return property;
        } catch (ScrapingFailedException e) {
            if (!fallbackEnabled) {
                throw e;
            }
            log.warn("Using mock property: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public Optional<PropertyDetailsDTO> getDetails(String url) {
        URI uri = finnUrlValidator.validate(url);
        if (!fallbackEnabled) {
            return Optional.empty();
        }
        return mock.details(uri);
    }
}
