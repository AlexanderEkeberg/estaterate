package no.weather.dto;

// snitt for én kalendermåned (1-12) over alle årene vi har komplette data for
public record MonthlyAverage(
        int month,
        double avgTempC,
        double avgMaxTempC,
        double avgMinTempC,
        double precipitationMm,
        double rainyDays,
        double snowfallCm,
        double sunshineHours,
        double avgMaxWindKmh,
        double frostDays
) {
}
