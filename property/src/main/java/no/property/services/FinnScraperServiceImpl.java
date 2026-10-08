package no.property.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.property.client.FinnClient;
import no.property.dto.PropertyLocationDTO;
import no.property.dto.ScrapeResponseDTO;
import no.property.validation.FinnUrlValidator;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinnScraperServiceImpl implements FinnScraperService {

    private final FinnClient finnClient;
    private final FinnHtmlParser finnAdHtmlParser;
    private final FinnUrlValidator finnUrlValidator;

    //Vurdere sener eom vi skal endre til database isteden for in-memory
    //TODO: fungerer kun med én kjørende instans, må løses før property skaleres
    private PropertyLocationDTO latest;

    @Override
    public ScrapeResponseDTO scrape(String url) {
        URI uri = finnUrlValidator.validate(url);
        String html = finnClient.fetchFinnHtml(uri);
        PropertyLocationDTO result = finnAdHtmlParser.parse(html, uri.toString());
        this.latest = result;

        String analysisId = UUID.randomUUID().toString();
        // vurdere senere publisher.publish.

        return new ScrapeResponseDTO(analysisId);
    }

    @Override
    public PropertyLocationDTO getLatest() {
        return latest;
    }
}
