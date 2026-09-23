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

import java.time.LocalDate;

@Slf4j
@Service
public class HistoricalWeatherClient {

    private static final String BASE_URL = "https://archive-api.open-meteo.com/v1/archive";

    private final RestTemplate restTemplate;

    public HistoricalWeatherClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public String getHistory(double lat, double lon) {
        LocalDate today = LocalDate.now();
        LocalDate oneYearAgo = today.minusYears(1);

        // bestmmer hvilke dataer vi henter ut fra apiet til open meteo.
        String url = UriComponentsBuilder.fromUriString(BASE_URL)
                .queryParam("latitude", lat)
                .queryParam("longitude", lon)
                .queryParam("start_date", oneYearAgo)
                .queryParam("end_date", today)
                .queryParam("daily", "temperature_2m_max,temperature_2m_min,precipitation_sum")
                .queryParam("timezone", "Europe/Oslo")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Could not contact Open-Meteo API", e);
            return null;
        }
    }
}
