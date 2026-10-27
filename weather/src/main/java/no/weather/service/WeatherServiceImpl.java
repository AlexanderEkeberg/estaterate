package no.weather.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.weather.client.HistoricalWeatherClient;
import no.weather.client.WeatherApiException;
import no.weather.client.YrClient;
import no.weather.dto.DailyForecast;
import no.weather.dto.DataSource;
import no.weather.dto.MonthlyAverage;
import no.weather.dto.MonthlyWeather;
import no.weather.dto.Sources;
import no.weather.dto.WeatherData;
import no.weather.events.PropertySubmittedEvent;
import no.weather.events.ScoreEvent;
import no.weather.mapper.WeatherMapper;
import no.weather.mock.MockWeatherData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private static final String CATEGORY = "WEATHER";

    private final YrClient yrClient;
    private final HistoricalWeatherClient historicalWeatherClient;
    private final WeatherMapper mapper;
    private final MockWeatherData mock;

    @Value("${weather.mock.fallback-enabled}")
    private boolean fallbackEnabled;

    @Value("${weather.mock.force:}")
    private String mockForce;

    @Override
    public ScoreEvent calculate(PropertySubmittedEvent event) {
        String analysisId = event.analysisId();
        if (event.latitude() == null || event.longitude() == null) {
            log.warn("Property has no coordinates: {}", analysisId);
            return ScoreEvent.failed(analysisId, CATEGORY, "Mangler koordinater");
        }

        try {
            double lat = event.latitude();
            double lon = event.longitude();

            // historikk: ekte data, eller mock hvis den ikke kunne hentes
            DataSource historySource = DataSource.LIVE;
            List<MonthlyAverage> averages;
            List<MonthlyWeather> monthly = liveMonthly(lat, lon);
            if (monthly == null) {
                historySource = DataSource.MOCK;
                monthly = mock.monthly();
                averages = mock.monthlyAverages();
            } else {
                averages = mapper.toMonthlyAverages(monthly);
            }

            // varsel: ekte data, eller mock hvis den ikke kunne hentes
            DataSource forecastSource = DataSource.LIVE;
            Instant forecastFetchedAt = Instant.now();
            List<DailyForecast> forecast = liveForecast(lat, lon);
            if (forecast == null) {
                forecastSource = DataSource.MOCK;
                forecastFetchedAt = null;
                forecast = mock.forecast();
            }

            WeatherData data = new WeatherData(new Sources(historySource, forecastSource),
                    event.coordinatesSource(), averages, monthly, forecast, forecastFetchedAt);
            return ScoreEvent.success(analysisId, CATEGORY, data);
        } catch (Exception e) {
            log.error("Weather failed for {}", analysisId, e);
            return ScoreEvent.failed(analysisId, CATEGORY, e.getMessage());
        }
    }

    // gir null hvis mock skal brukes
    private List<MonthlyWeather> liveMonthly(double lat, double lon) {
        if (mockForce.contains("history")) {
            return null;
        }
        try {
            List<MonthlyWeather> monthly = mapper.toMonthly(historicalWeatherClient.getHistory(lat, lon));
            if (mapper.toMonthlyAverages(monthly).isEmpty()) {
                throw new WeatherApiException("Open-Meteo had too little data for monthly averages");
            }
            return monthly;
        } catch (WeatherApiException e) {
            checkFallbackEnabled(e, "history");
            return null;
        }
    }

    // gir null hvis mock skal brukes
    private List<DailyForecast> liveForecast(double lat, double lon) {
        if (mockForce.contains("forecast")) {
            return null;
        }
        try {
            List<DailyForecast> forecast = mapper.toForecast(yrClient.getForecast(lat, lon));
            if (forecast.isEmpty()) {
                throw new WeatherApiException("Yr had no usable forecast days");
            }
            return forecast;
        } catch (WeatherApiException e) {
            checkFallbackEnabled(e, "forecast");
            return null;
        }
    }

    // er fallback slått av, blir feilen til en FAILED-melding
    private void checkFallbackEnabled(WeatherApiException e, String part) {
        if (!fallbackEnabled) {
            throw e;
        }
        log.warn("Using mock {}: {}", part, e.getMessage());
    }
}
