package no.district.services;

import lombok.RequiredArgsConstructor;
import no.district.clients.PropertyClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DistrictServiceImpl implements DistrictService {

    private final PropertyClient propertyClient;

    @Override
    public String getDistrictData() {
        String propertyResponse = propertyClient.getPropertyData();
        return "Received: " + propertyResponse;
    }

}
