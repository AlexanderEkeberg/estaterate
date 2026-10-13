package no.school.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)

public class NsrUnitDTO {
    @JsonProperty("Organisasjonsnummer")
    private String organizationNumber;

    @JsonProperty("Navn")
    private String name;

    @JsonProperty("ErAktiv")
    private boolean active;

    @JsonProperty("ErGrunnskole")
    private boolean primarySchool;

    @JsonProperty("Koordinat")
    private Koordinat coordinate;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Koordinat {
        @JsonProperty("Lengdegrad")
        private Double longitude;

        @JsonProperty("Breddegrad")
        private Double latitude;

        @JsonProperty("GeoKilde")
        private String geoSource;

    }}
