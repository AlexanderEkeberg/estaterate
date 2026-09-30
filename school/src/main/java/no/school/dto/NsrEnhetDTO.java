package no.school.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)

public class NsrEnhetDTO {
    @JsonProperty("Organisasjonsnummer")
    private String organisasjonsnummer;

    @JsonProperty("Navn")
    private String navn;

    @JsonProperty("ErAktiv")
    private boolean erAktiv;

    @JsonProperty("ErGrunnskole")
    private boolean erGrunnskole;

    @JsonProperty("Koordinat")
    private Koordinat koordinat;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Koordinat {
        @JsonProperty("Lengdegrad")
        private Double lengdegrad;

        @JsonProperty("Breddegrad")
        private Double breddegrad;

        @JsonProperty("GeoKilde")
        private String geoKilde;

    }}
