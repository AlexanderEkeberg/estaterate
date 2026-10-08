package no.property.kafka;

import lombok.RequiredArgsConstructor;
import no.property.event.PropertyEventPublisher;
import no.property.event.PropertySubmittedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaPropertyEventPublisher implements PropertyEventPublisher {

    private static final String TOPIC = "property-submitted";

    private final KafkaTemplate<String, PropertySubmittedEvent> kafkaTemplate;

    @Override
    public void publish(PropertySubmittedEvent event) {
        kafkaTemplate.send(TOPIC, event.analysisId(), event);
    }
}
