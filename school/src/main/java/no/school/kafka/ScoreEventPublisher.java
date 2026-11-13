package no.school.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.school.events.ScoreEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ScoreEventPublisher {

    private static final String TOPIC = "score-events";

    private final KafkaTemplate<String, ScoreEvent> kafkaTemplate;

    public void publish(ScoreEvent event) {
        try {
            kafkaTemplate.send(TOPIC, event.analysisId(), event).whenComplete((result, error) -> {
                if (error != null) {
                    log.error("Could not send score event {}", event.analysisId(), error);
                } else {
                    log.info("Sent score event: {} {}", event.analysisId(), event.status());
                }
            });
        } catch (Exception e) {
            log.error("Could not send score event {}", event.analysisId(), e);
        }
    }
}
