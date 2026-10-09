package no.property.kafka;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import no.property.exception.EventPublishFailedException;
import no.property.event.PropertyEventPublisher;
import no.property.event.PropertySubmittedEvent;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaPropertyEventPublisher implements PropertyEventPublisher {

    private static final String TOPIC = "property-submitted";

    private final KafkaTemplate<String, PropertySubmittedEvent> kafkaTemplate;

    @Override
    public void publish(PropertySubmittedEvent event) {
        try {
            kafkaTemplate.send(TOPIC, event.analysisId(), event).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EventPublishFailedException("Interrupted while publishing event", e);
        } catch (ExecutionException | TimeoutException | org.apache.kafka.common.KafkaException | KafkaException e) {
            throw new EventPublishFailedException("Could not publish event " + event.analysisId(), e);
        }
    }
}
