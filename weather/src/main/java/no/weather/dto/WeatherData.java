package no.weather.dto;

import java.time.Instant;
import java.util.List;

public record WeatherData(
        List<MonthlyWeather> monthly,
        List<DailyForecast> forecast,
        Instant forecastFetchedAt
) {
}
