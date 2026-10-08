package no.property.client;

import lombok.extern.slf4j.Slf4j;
import no.property.exception.ScrapingFailedException;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Slf4j
@Service
public class FinnClient {
    private final RestTemplate restTemplate;

    public FinnClient(RestTemplateBuilder Builder) {
        this.restTemplate = Builder.build();
    }

    // TODO: sett connect/read-timeout og slå av redirects
    public String fetchFinnHtml(URI uri) {
        try {
            String html = restTemplate.getForObject(uri, String.class);
            if (html == null || html.isBlank()) {
                throw new ScrapingFailedException("Empty response from FINN");
            }
            return html;
        } catch (RestClientException e) {
            throw new ScrapingFailedException("Could not fetch listing", e);
        }
    }
}
