package no.weather.client;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class YrClient {

    private static final String BASE_URL =
            "https://api.met.no/weatherapi/locationforecast/2.0/compact";

    private final RestTemplate restTemplate;

    public YrClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public String getForecast(double lat, double lon) {
        String url = UriComponentsBuilder.fromUriString(BASE_URL)
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "estaterate-weather/0.1");
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Could not contact api", e);
            return null;
        }
    }
}