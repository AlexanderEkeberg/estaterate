package no.property.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyLocationDTO {
    private String url;
    private String postalCode;
    private double latitude;
    private double longitude;
    private String municipalityNumber;
    private String district;
}
