package no.property.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.property.client.FinnClient;
import no.property.dto.PropertyLocationDTO;
import no.property.validation.FinnUrlValidator;
import org.springframework.stereotype.Service;

import java.net.URI;

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
    public PropertyLocationDTO scrape(String url) {
        URI uri = finnUrlValidator.validate(url);
        String html = finnClient.fetchFinnHtml(uri);
        PropertyLocationDTO result = finnAdHtmlParser.parse(html, uri.toString());
        this.latest = result;
        return result;
    }

    @Override
    public PropertyLocationDTO getLatest() {
        return latest;
    }
}
