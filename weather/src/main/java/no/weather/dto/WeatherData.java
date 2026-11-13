package no.weather.dto;

import java.time.Instant;
import java.util.List;

public record WeatherData(
        Sources sources,
        String locationSource,
        List<MonthlyAverage> monthlyAverages,
        List<MonthlyWeather> monthly,
        List<DailyForecast> forecast,
        Instant forecastFetchedAt
) {
}
