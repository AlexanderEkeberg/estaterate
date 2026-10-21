package no.weather.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.weather.client.HistoricalWeatherClient;
import no.weather.client.PropertyClient;
import no.weather.client.YrClient;
import no.weather.client.dto.YrResponse;
import no.weather.dto.WeatherResponseDTO;
import no.weather.events.PropertySubmittedEvent;
import no.weather.kafka.LatestPropertyState;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final PropertyClient propertyClient;
    private final YrClient yrClient;
    private final HistoricalWeatherClient historicalWeatherClient;
    private final LatestPropertyState latestPropertyState;

    // Byttet ut med asynchronous kommunikasjon
//    @Override
//    public WeatherResponseDTO getWeather() {
//        PropertyLocationDTO location = propertyClient.getPropertyLocation();
//        if (location == null || location.getLatitude() == null ||
//                location.getLongitude() == null) {
//            log.warn("Ingen lokasjonsdata tilgjengelig fra property");
//            return null;
//        }
//        double lat = Double.parseDouble(location.getLatitude());
//        double lon = Double.parseDouble(location.getLongitude());
//
//        String forecast = yrClient.getForecast(lat, lon);
//        String history = historicalWeatherClient.getHistory(lat, lon);
//
//        return new WeatherResponseDTO(forecast, history);
//    }

    @Override
    public WeatherResponseDTO getWeather() {
        PropertySubmittedEvent event = latestPropertyState.getLatest();
        if (event == null || event.latitude() == null || event.longitude() == null) {
            log.warn("Ingen lokasjonsdata tilgjengelig fra property");
            return null;
        }
        double lat = event.latitude();
        double lon = event.longitude();

        YrResponse forecast = yrClient.getForecast(lat, lon);
        String history = historicalWeatherClient.getHistory(lat, lon);

        return new WeatherResponseDTO(forecast, history);
    }
}
