package no.weather.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.weather.events.PropertySubmittedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class PropertySubmittedConsumer {

    private final LatestPropertyState latestPropertyState;

    @KafkaListener(
            topics = "property-submitted",
            groupId = "weather-service"
    )
    public void receive(PropertySubmittedEvent event) {
        log.info("Received property event: {}", event);
        latestPropertyState.update(event);

        if (event.latitude() == null || event.longitude() == null) {
            log.warn("Property has no coordinates: {}", event.propertyId());
        }

    }
}