package no.weather.kafka;

import lombok.extern.slf4j.Slf4j;
import no.weather.events.PropertySubmittedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PropertySubmittedConsumer {

    @KafkaListener(
            topics = "property-submitted",
            groupId = "weather-service"
    )
    public void receive(PropertySubmittedEvent event) {
        log.info("Received property event: {}", event);

        if (event.latitude() == null || event.longitude() == null) {
            log.warn("Property has no coordinates: {}", event.propertyId());
        }

    }
}