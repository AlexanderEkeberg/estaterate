package org.example.school.service;

import lombok.RequiredArgsConstructor;
import org.example.school.client.PropertyClient;
import org.example.school.dto.PropertyLocationDTO;
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
