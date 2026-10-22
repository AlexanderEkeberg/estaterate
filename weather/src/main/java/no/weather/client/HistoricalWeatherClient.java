package no.weather.client;

import lombok.extern.slf4j.Slf4j;
import no.weather.client.dto.OpenMeteoResponse;
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
    private static final int HISTORY_YEARS = 5;
    private static final int ARCHIVE_DELAY_DAYS = 5;

    private final RestTemplate restTemplate;

    public HistoricalWeatherClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public OpenMeteoResponse getHistory(double lat, double lon) {
        // arkivet ligger noen dager bak, så siste dagene kan mangle data
        LocalDate endDate = LocalDate.now().minusDays(ARCHIVE_DELAY_DAYS);
        LocalDate startDate = endDate.minusYears(HISTORY_YEARS);

        // bestmmer hvilke dataer vi henter ut fra apiet til open meteo.
        String url = UriComponentsBuilder.fromUriString(BASE_URL)
                .queryParam("latitude", lat)
                .queryParam("longitude", lon)
                .queryParam("start_date", startDate)
                .queryParam("end_date", endDate)
                .queryParam("daily", "temperature_2m_max,temperature_2m_min,precipitation_sum")
                .queryParam("timezone", "Europe/Oslo")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<OpenMeteoResponse> response =
                    restTemplate.exchange(url, HttpMethod.GET, request, OpenMeteoResponse.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Could not contact Open-Meteo API", e);
            return null;
        }
    }
}
