package org.example.crimerate.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.crimerate.events.PropertySubmittedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class PropertySubmittedConsumer {

    private final LatestPropertyState latestPropertyState;

    @KafkaListener(
            topics = "property-submitted",
            groupId = "crimerate-service"
    )
    public void receive(PropertySubmittedEvent event) {
        log.info("Received property event: {}", event);
        latestPropertyState.update(event);
        if (event.district() == null) {
            log.warn("Property has no district: {}", event.analysisId());
        }
    }
}
