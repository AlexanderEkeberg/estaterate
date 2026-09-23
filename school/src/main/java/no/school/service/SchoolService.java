package no.school.service;

import lombok.RequiredArgsConstructor;
import no.school.client.PropertyClient;
import no.school.dto.PropertyLocationDTO;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchoolService {

    private final PropertyClient propertyClient;

    public String propertyConnection() {
        PropertyLocationDTO location = propertyClient.getPropertyLocation();
        return location != null
                ? "Got location: " + location.getLatitude() + ", " + location.getLongitude()
                : "Could not reach property service";
    }
}
