package no.school.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NsrEnhetResponseDTO {
    @JsonProperty("TotaltAntallEnheter")
    private int totaltAntallEnheter;

    @JsonProperty("EnhetListe")
    private List<NsrEnhetDTO> enhetListe;

}
