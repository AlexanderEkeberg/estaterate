package no.property.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.property.client.FinnClient;
import no.property.dto.PropertyLocationDTO;
import no.property.dto.ScrapeResponseDTO;
import no.property.event.PropertyEventPublisher;
import no.property.event.PropertySubmittedEvent;
import no.property.validation.FinnUrlValidator;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinnScraperServiceImpl implements FinnScraperService {

    private final FinnClient finnClient;
    private final FinnHtmlParser finnAdHtmlParser;
    private final FinnUrlValidator finnUrlValidator;
    private final PropertyEventPublisher eventPublisher;

    // TODO: fjernes sammen med GET /api/properties når crimerate og school bruker Kafka.
    // Beholdes midlertidig fordi de fortsatt henter data over REST.
    private PropertyLocationDTO latest;

    @Override
    public ScrapeResponseDTO scrape(String url) {
        URI uri = finnUrlValidator.validate(url);
        String html = finnClient.fetchFinnHtml(uri);
        PropertyLocationDTO result = finnAdHtmlParser.parse(html, uri.toString());
        this.latest = result;

        String analysisId = UUID.randomUUID().toString();

        eventPublisher.publish(new PropertySubmittedEvent(
                UUID.randomUUID(),
                "PropertySubmitted",
                1,
                Instant.now(),
                analysisId,
                result.getUrl(),
                result.getPostalCode(),
                result.getLatitude(),
                result.getLongitude(),
                result.getMunicipalityNumber(),
                result.getDistrict()));

        return new ScrapeResponseDTO(analysisId);
    }

    @Override
    public PropertyLocationDTO getLatest() {
        return latest;
    }
}
