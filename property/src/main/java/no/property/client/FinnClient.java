package no.property.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class FinnClient {
    private final RestTemplate restTemplate;

    public FinnClient(RestTemplateBuilder Builder) {
        this.restTemplate = Builder.build();
    }

    public String fetchFinnHtml(String url) {
        //TODO: try/catch burde legges til
        return restTemplate.getForObject(url, String.class);
    }
}
