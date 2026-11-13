package no.weather.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoResponse(Daily daily, Hourly hourly) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Daily(
            List<LocalDate> time,
            @JsonProperty("temperature_2m_mean") List<Double> temperatureMean,
            @JsonProperty("temperature_2m_max") List<Double> temperatureMax,
            @JsonProperty("temperature_2m_min") List<Double> temperatureMin,
            @JsonProperty("precipitation_sum") List<Double> precipitationSum,
            @JsonProperty("snowfall_sum") List<Double> snowfallSum,
            @JsonProperty("wind_speed_10m_max") List<Double> windSpeedMax
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Hourly(
            List<LocalDateTime> time,
            @JsonProperty("direct_normal_irradiance") List<Double> directNormalIrradiance,
            @JsonProperty("cloud_cover") List<Double> cloudCover
    ) {
    }
}
