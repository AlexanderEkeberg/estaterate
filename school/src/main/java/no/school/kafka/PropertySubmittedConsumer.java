package no.school.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.school.events.PropertySubmittedEvent;
import no.school.service.SchoolService;
import org.springframework.stereotype.Service;
import org.springframework.kafka.annotation.KafkaListener;

@Slf4j
@RequiredArgsConstructor
@Service

public class PropertySubmittedConsumer {

    private final SchoolService schoolService;
    private final ScoreEventPublisher publisher;


    @KafkaListener(
            topics = "property-submitted",
            groupId = "school-service"
    )
    public void receive(PropertySubmittedEvent event) {
        log.atInfo()
                .addKeyValue("event_type", "property_received")
                .addKeyValue("analysis_id", event.analysisId())
                .addKeyValue("latitude", event.latitude())
                .addKeyValue("longitude", event.longitude())
                .log("Received property event");
        publisher.publish(schoolService.calculate(event));

    }
}
