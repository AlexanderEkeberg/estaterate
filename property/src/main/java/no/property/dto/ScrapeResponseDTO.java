package no.property.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.property.event.CoordinatesSource;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScrapeResponseDTO {
    private String analysisId;
    private CoordinatesSource coordinatesSource;
    private String message;
}
