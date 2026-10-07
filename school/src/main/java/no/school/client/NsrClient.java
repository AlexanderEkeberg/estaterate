package no.school.client;

import lombok.extern.slf4j.Slf4j;
import no.school.dto.NsrEnhetDTO;
import no.school.dto.NsrEnhetResponseDTO;
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

    public NsrEnhetResponseDTO getSchoolsByKommune(String kommunenummer) {
        String url = BASE_URL + "/enheter/kommune/" + kommunenummer;
        try {
            return restTemplate.getForObject(url, NsrEnhetResponseDTO.class);
        } catch (Exception e) {
            log.error("kunne ikke nå nsr-api(kommune list)", e);
            return null;
        }
    }

    public NsrEnhetDTO getSchoolDetail(String organisasjonsnummer) {
        String url = BASE_URL + "/enhet/" + organisasjonsnummer;
        try {
            return restTemplate.getForObject(url, NsrEnhetDTO.class);
        } catch (Exception e) {
            log.error("kunne ikke nå nsr-api (enhet detail)", e);
            return null;
        }
    }

}
