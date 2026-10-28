package no.weather.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YrResponse(Properties properties) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Properties(List<TimeSeries> timeseries) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TimeSeries(Instant time, Data data) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(
            InstantData instant,
            @JsonProperty("next_1_hours") Period next1Hours,
            @JsonProperty("next_6_hours") Period next6Hours
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InstantData(InstantDetails details) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InstantDetails(@JsonProperty("air_temperature") Double airTemperature) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Period(Summary summary, PeriodDetails details) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Summary(@JsonProperty("symbol_code") String symbolCode) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PeriodDetails(
            @JsonProperty("precipitation_amount") Double precipitationAmount,
            @JsonProperty("air_temperature_max") Double airTemperatureMax,
            @JsonProperty("air_temperature_min") Double airTemperatureMin
    ) {
    }
}
