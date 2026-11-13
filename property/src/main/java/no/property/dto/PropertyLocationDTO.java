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
    private Double latitude;
    private Double longitude;
    private String municipalityNumber;
    private String district;

    public boolean hasRequiredFields() {
        return latitude != null && longitude != null && postalCode != null && municipalityNumber != null;
    }
}
