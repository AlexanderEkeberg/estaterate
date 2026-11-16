package org.example.crimerate.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.crimerate.events.PropertySubmittedEvent;
import org.example.crimerate.service.CrimeStatsService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class PropertySubmittedConsumer {

    private final CrimeStatsService crimeStatsService;
    private final ScoreEventPublisher publisher;

    @KafkaListener(
            topics = "property-submitted",
            groupId = "crimerate-service"
    )
    public void receive(PropertySubmittedEvent event) {
        log.atInfo()
                .addKeyValue("event_type", "property_received")
                .addKeyValue("analysis_id", event.analysisId())
                .addKeyValue("district", event.district())
                .log("Received property event");
        publisher.publish(crimeStatsService.calculate(event));
    }
}
