package no.weather.dto;

import lombok.AllArgsConstructor;
import no.weather.client.dto.YrResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponseDTO {
    private YrResponse forecast;
    private String history;
}
