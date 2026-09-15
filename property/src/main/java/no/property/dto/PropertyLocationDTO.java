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
    //disse er for koordinatene for weather
    //Todo: endre til double
    private String latitude;
    private String longitude;
}
