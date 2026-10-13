package no.school.client;

import lombok.extern.slf4j.Slf4j;
import no.school.dto.PropertyLocationDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class PropertyClient {

    private final String url;
    private final RestTemplate restTemplate;

    public PropertyClient(RestTemplateBuilder builder, @Value("${property.service.url}") String url) {
        this.url = url;
        this.restTemplate = builder.build();
    }

    public PropertyLocationDTO getPropertyLocation() {
        try {
            return restTemplate.getForObject(url, PropertyLocationDTO.class);
        } catch (Exception e) {
            log.error("Kan ikke nå Property Service", e);
            return null;
        }
    }
}