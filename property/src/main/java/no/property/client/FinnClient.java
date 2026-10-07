package no.property.client;

import lombok.extern.slf4j.Slf4j;
import no.property.exception.ScrapingFailedException;
import org.springframework.boot.http.client.HttpRedirects;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Duration;

@Slf4j
@Service
public class FinnClient {
    private final RestTemplate restTemplate;

    public FinnClient(RestTemplateBuilder Builder) {
        this.restTemplate = Builder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .redirects(HttpRedirects.DONT_FOLLOW)
                .build();
    }
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
