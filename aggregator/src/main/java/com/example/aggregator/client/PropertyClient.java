package com.example.aggregator.client;

import com.example.aggregator.dto.PropertyDetailsDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@Service
public class PropertyClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public PropertyClient(
            RestTemplateBuilder builder,
            @Value("${property.service.url:http://localhost:8090/api/properties}") String baseUrl
    ) {
        this.restTemplate = builder.build();
        this.baseUrl = baseUrl;
    }

    public Optional<PropertyDetailsDTO> getDetails(String url) {
        URI uri = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/details")
                .queryParam("url", url)
                .encode()
                .build()
                .toUri();

        try {
            return Optional.ofNullable(restTemplate.getForObject(uri, PropertyDetailsDTO.class));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }
}