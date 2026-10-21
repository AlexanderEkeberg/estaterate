package no.weather.dto;

public record MonthlyWeather(
        int year,
        int month,
        double avgTempC,
        double precipitationMm
) {
}
