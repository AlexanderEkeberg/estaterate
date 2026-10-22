package no.weather.dto;

public record MonthlyWeather(
        int year,
        int month,
        double avgTempC,
        double avgMaxTempC,
        double avgMinTempC,
        double precipitationMm,
        int rainyDays,
        double snowfallCm,
        double sunshineHours,
        double avgMaxWindKmh,
        int frostDays,
        boolean complete
) {
}
