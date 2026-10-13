package no.school.client;

import lombok.extern.slf4j.Slf4j;
import no.school.dto.NsrUnitDTO;
import no.school.dto.NsrUnitResponseDTO;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service

public class NsrClient {
    private static final String BASE_URL = "https://data-nsr.udir.no/v4";

    private final RestTemplate restTemplate;

    public NsrClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public NsrUnitResponseDTO getSchoolsByMunicipality(String municipalityNumber) {
        String url = BASE_URL + "/enheter/kommune/" + municipalityNumber;
        try {
            return restTemplate.getForObject(url, NsrUnitResponseDTO.class);
        } catch (Exception e) {
            log.error("kunne ikke nå nsr-api", e);
            return null;
        }
    }

    public NsrUnitDTO getSchoolDetail(String organizationNumber) {
        String url = BASE_URL + "/enhet/" + organizationNumber;
        try {
            return restTemplate.getForObject(url, NsrUnitDTO.class);
        } catch (Exception e) {
            log.error("kunne ikke nå nsr-api", e);
            return null;
        }
    }

}
