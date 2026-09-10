package no.district.clients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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

    public String getPropertyData() {
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
                    }
            );
            return response.getBody();
        }
        catch (Exception e) {
            log.error("Could not contact Property Service", e);
            return null;
        }
    }


}