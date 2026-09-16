package no.weather.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.weather.client.PropertyClient;
import no.weather.client.YrClient;
import no.weather.dto.PropertyLocationDTO;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final PropertyClient propertyClient;
    private final YrClient yrClient;

    @Override
    public String getWeather() {
        PropertyLocationDTO location = propertyClient.getPropertyLocation();
        if (location == null || location.getLatitude() == null ||
                location.getLongitude() == null) {
            log.warn("Ingen lokasjonsdata tilgjengelig fra property");
            return null;
        }
        double lat = Double.parseDouble(location.getLatitude());
        double lon = Double.parseDouble(location.getLongitude());

        return yrClient.getForecast(lat, lon);
    }
}
