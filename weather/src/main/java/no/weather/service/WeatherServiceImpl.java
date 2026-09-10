package no.weather.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.weather.client.PropertyClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final PropertyClient propertyClient;

    @Override
    public String getWeather() {
        String propertyData = propertyClient.getPropertyData();
        log.info("propertyData: {}", propertyData);

        return propertyData;
    }
}
