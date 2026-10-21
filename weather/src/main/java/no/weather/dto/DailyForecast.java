package no.weather.dto;

import java.time.LocalDate;

public record DailyForecast(
        LocalDate date,
        double minTempC,
        double maxTempC,
        double precipitationMm,
        String symbol
) {
}
