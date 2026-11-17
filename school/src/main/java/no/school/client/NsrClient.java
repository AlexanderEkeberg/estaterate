package no.school.client;

import lombok.extern.slf4j.Slf4j;
import no.school.dto.NsrUnitDTO;
import no.school.dto.NsrUnitResponseDTO;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Slf4j
@Service

public class NsrClient {
    private static final String BASE_URL = "https://data-nsr.udir.no/v4";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

    private final RestTemplate restTemplate;

    public NsrClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.connectTimeout(CONNECT_TIMEOUT).readTimeout(READ_TIMEOUT).build();
    }

    public NsrUnitResponseDTO getSchoolsByMunicipality(String municipalityNumber) {
        String url = BASE_URL + "/enheter/kommune/" + municipalityNumber;
        try {
            return HttpRetry.call(() -> restTemplate.getForObject(url, NsrUnitResponseDTO.class));
        } catch (Exception e) {
            log.atWarn()
                    .setCause(e)
                    .addKeyValue("event_type", "nsr_unavailable")
                    .addKeyValue("url", url)
                    .log("Could not reach the NSR API");
            return null;
        }
    }

    public NsrUnitDTO getSchoolDetail(String organizationNumber) {
        String url = BASE_URL + "/enhet/" + organizationNumber;
        try {
            return HttpRetry.call(() -> restTemplate.getForObject(url, NsrUnitDTO.class));
        } catch (Exception e) {
            log.atWarn()
                    .addKeyValue("event_type", "nsr_unavailable")
                    .addKeyValue("url", url)
                    .addKeyValue("reason", e.getMessage())
                    .log("Could not reach the NSR API");
            return null;
        }
    }

}
