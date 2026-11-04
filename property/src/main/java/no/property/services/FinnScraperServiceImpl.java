package no.property.services;

import lombok.extern.slf4j.Slf4j;
import no.property.client.FinnClient;
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
import java.util.UUID;

@Slf4j
@Service
public class FinnScraperServiceImpl implements FinnScraperService {

    static final String MOCK_MESSAGE = "We could not read the listing. The report shows sample data from Oslo.";

    private final FinnClient finnClient;
    private final FinnHtmlParser finnAdHtmlParser;
    private final FinnUrlValidator finnUrlValidator;
    private final PropertyEventPublisher eventPublisher;
    private final MockPropertyData mock;
    private final boolean fallbackEnabled;
    private final boolean mockForced;

    // TODO: fjernes sammen med GET /api/properties når school bruker Kafka.
    // Beholdes midlertidig fordi school fortsatt henter data over REST.
    private PropertyLocationDTO latest;

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
        PropertyLocationDTO property = scrapeOrNull(uri);
        if (property == null) {
            source = CoordinatesSource.MOCK;
            property = mock.property(uri.toString());
        }
        this.latest = property;

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

        String message = source == CoordinatesSource.MOCK ? MOCK_MESSAGE : null;
        return new ScrapeResponseDTO(analysisId, source, message);
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
    public PropertyLocationDTO getLatest() {
        return latest;
    }
}
