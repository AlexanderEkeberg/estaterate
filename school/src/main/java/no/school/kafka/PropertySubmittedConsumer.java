package no.school.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.school.events.PropertySubmittedEvent;
import org.springframework.stereotype.Service;
import org.springframework.kafka.annotation.KafkaListener;

@Slf4j
@RequiredArgsConstructor
@Service

public class PropertySubmittedConsumer {

    private final LatestPropertyState latestPropertyState;

    @KafkaListener(
            topics = "property-submitted",
            groupId = "school-service"
    )
    public void receive(PropertySubmittedEvent event) {
        log.info("Received property event: {}", event);
        latestPropertyState.update(event);
        if (event.latitude() == null || event.longitude() == null) {
            log.warn("Property has no coordinates: {}", event.analysisId());
        }
    }


}
