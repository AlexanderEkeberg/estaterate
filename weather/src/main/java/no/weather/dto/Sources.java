package no.weather.dto;

public record Sources(
        DataSource history,
        DataSource forecast
) {
}
