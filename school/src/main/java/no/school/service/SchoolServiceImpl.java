package no.school.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.school.client.PropertyClient;
import no.school.dto.PropertyLocationDTO;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {

    private final PropertyClient propertyClient;

    @Override
    public String propertyConnection() {
        PropertyLocationDTO location = propertyClient.getPropertyLocation();
        if (location == null || location.getLatitude() == null || location.getLongitude() == null) {
            log.warn("Ingen lokasjonsdata tilgjengelig fra property");
            return "Kunne ikke nå property";
        }
        double lat = Double.parseDouble(location.getLatitude());
        double lon = Double.parseDouble(location.getLongitude());

        return "Fant lokasjon: " + lat + ", " + lon;
    }
}