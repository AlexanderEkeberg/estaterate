package no.weather.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.weather.events.PropertySubmittedEvent;
import no.weather.service.WeatherService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class PropertySubmittedConsumer {

    private final WeatherService weatherService;
    private final ScoreEventPublisher publisher;

    @KafkaListener(
            topics = "property-submitted",
            groupId = "weather-service"
    )
    public void receive(PropertySubmittedEvent event) {
        log.atInfo()
                .addKeyValue("event_type", "property_received")
                .addKeyValue("analysis_id", event.analysisId())
                .addKeyValue("latitude", event.latitude())
                .addKeyValue("longitude", event.longitude())
                .log("Received property event");
        publisher.publish(weatherService.calculate(event));
    }
}