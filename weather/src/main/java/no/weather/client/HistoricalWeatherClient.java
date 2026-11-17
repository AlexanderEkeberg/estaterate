package no.weather.client;

import lombok.extern.slf4j.Slf4j;
import no.weather.client.dto.OpenMeteoResponse;
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
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class HistoricalWeatherClient {

    private static final int HISTORY_YEARS = 5;
    private static final int ARCHIVE_DELAY_DAYS = 5;
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

    private static final String DAILY_VARIABLES = String.join(",",
            "temperature_2m_mean",
            "temperature_2m_max",
            "temperature_2m_min",
            "precipitation_sum",
            "snowfall_sum",
            "wind_speed_10m_max");

    private static final String HOURLY_VARIABLES = String.join(",",
            "direct_normal_irradiance",
            "cloud_cover");

    private final String baseUrl;
    private final RestTemplate restTemplate;

    @Autowired
    public HistoricalWeatherClient(RestTemplateBuilder builder,
                                   @Value("${openmeteo.archive.url}") String baseUrl) {
        this(builder.connectTimeout(CONNECT_TIMEOUT).readTimeout(READ_TIMEOUT).build(), baseUrl);
    }

    // brukes i testene, slik at de kan sette inn en RestTemplate med mock-server
    HistoricalWeatherClient(RestTemplate restTemplate, String baseUrl) {
        this.baseUrl = baseUrl;
        this.restTemplate = restTemplate;
    }

    public OpenMeteoResponse getHistory(double lat, double lon) {
        // arkivet ligger noen dager bak, så siste dagene kan mangle data
        LocalDate endDate = LocalDate.now().minusDays(ARCHIVE_DELAY_DAYS);
        // hele kalenderår fra 1. januar, slik at månedssnittene blir riktige
        LocalDate startDate = LocalDate.of(endDate.getYear() - HISTORY_YEARS, 1, 1);

        // bestmmer hvilke dataer vi henter ut fra apiet til open meteo.
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("latitude", lat)
                .queryParam("longitude", lon)
                .queryParam("start_date", startDate)
                .queryParam("end_date", endDate)
                .queryParam("daily", DAILY_VARIABLES)
                .queryParam("hourly", HOURLY_VARIABLES)
                .queryParam("timezone", "Europe/Oslo")
                .toUriString();

        HttpEntity<Void> request = new HttpEntity<>(new HttpHeaders());

        OpenMeteoResponse body;
        try {
            body = HttpRetry.call(() ->
                    restTemplate.exchange(url, HttpMethod.GET, request, OpenMeteoResponse.class).getBody());
        } catch (RestClientException e) {
            log.atWarn()
                    .addKeyValue("event_type", "open_meteo_unavailable")
                    .addKeyValue("url", url)
                    .addKeyValue("reason", e.getMessage())
                    .log("Could not contact Open-Meteo API");
            throw new WeatherApiException("Could not contact Open-Meteo API", e);
        }
        validate(body);
        return body;
    }

    private void validate(OpenMeteoResponse body) {
        if (body == null || body.daily() == null) {
            throw new WeatherApiException("Open-Meteo returned no daily data");
        }
        OpenMeteoResponse.Daily daily = body.daily();
        if (daily.time() == null || daily.time().isEmpty()) {
            throw new WeatherApiException("Open-Meteo returned no days");
        }
        int days = daily.time().size();
        List<List<Double>> series = List.of(
                nullToEmpty(daily.temperatureMean()),
                nullToEmpty(daily.temperatureMax()),
                nullToEmpty(daily.temperatureMin()),
                nullToEmpty(daily.precipitationSum()),
                nullToEmpty(daily.snowfallSum()),
                nullToEmpty(daily.windSpeedMax()));
        if (series.stream().anyMatch(values -> values.size() != days)) {
            throw new WeatherApiException("Open-Meteo returned daily lists of different length");
        }

        OpenMeteoResponse.Hourly hourly = body.hourly();
        if (hourly == null || hourly.time() == null || hourly.time().isEmpty()) {
            throw new WeatherApiException("Open-Meteo returned no hourly data");
        }
        int hours = hourly.time().size();
        if (nullToEmpty(hourly.directNormalIrradiance()).size() != hours
                || nullToEmpty(hourly.cloudCover()).size() != hours) {
            throw new WeatherApiException("Open-Meteo returned hourly lists of different length");
        }
    }

    private static List<Double> nullToEmpty(List<Double> values) {
        return values == null ? List.of() : values;
    }
}
