package no.weather.client;


import lombok.extern.slf4j.Slf4j;
import no.weather.client.dto.YrResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;

@Slf4j
@Service
public class YrClient {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(8);

    private final String baseUrl;
    private final RestTemplate restTemplate;

    @Autowired
    public YrClient(RestTemplateBuilder builder, @Value("${yr.forecast.url}") String baseUrl) {
        this(builder.connectTimeout(CONNECT_TIMEOUT).readTimeout(READ_TIMEOUT).build(), baseUrl);
    }

    // brukes i testene, slik at de kan sette inn en RestTemplate med mock-server
    YrClient(RestTemplate restTemplate, String baseUrl) {
        this.baseUrl = baseUrl;
        this.restTemplate = restTemplate;
    }

    public YrResponse getForecast(double lat, double lon) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "estaterate-weather/0.1");
        HttpEntity<Void> request = new HttpEntity<>(headers);

        YrResponse body;
        try {
            body = HttpRetry.call(() ->
                    restTemplate.exchange(url, HttpMethod.GET, request, YrResponse.class).getBody());
        } catch (RestClientException e) {
            log.atWarn()
                    .addKeyValue("event_type", "yr_unavailable")
                    .addKeyValue("url", url)
                    .addKeyValue("reason", e.getMessage())
                    .log("Could not contact Yr API");
            throw new WeatherApiException("Could not contact Yr API", e);
        }
        validate(body);
        return body;
    }

    private void validate(YrResponse body) {
        if (body == null || body.properties() == null
                || body.properties().timeseries() == null
                || body.properties().timeseries().isEmpty()) {
            throw new WeatherApiException("Yr returned no forecast data");
        }
    }
}
