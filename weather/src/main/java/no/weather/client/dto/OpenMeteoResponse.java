package no.weather.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoResponse(Daily daily) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Daily(
            List<LocalDate> time,
            @JsonProperty("temperature_2m_max") List<Double> temperatureMax,
            @JsonProperty("temperature_2m_min") List<Double> temperatureMin,
            @JsonProperty("precipitation_sum") List<Double> precipitationSum
    ) {
    }
}
