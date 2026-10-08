package no.property.kafka;

import lombok.RequiredArgsConstructor;
import no.property.events.PropertySubmittedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PropertySubmittedProducer {

    private static final String TOPIC = "property-submitted";

    private final KafkaTemplate<String, PropertySubmittedEvent> kafkaTemplate;

    public void send(PropertySubmittedEvent event) {
        kafkaTemplate.send(
                TOPIC,
                event.propertyId().toString(),
                event
        );
    }
}