package no.weather.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.weather.events.ScoreEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ScoreEventPublisher {
    private static final String TOPIC = "score-events";

    private final KafkaTemplate<String, ScoreEvent> kafkaTemplate;

    public void publish(ScoreEvent event) {
        kafkaTemplate.send(TOPIC, event.analysisId(), event);
        log.info("Sent score event: {} {}", event.analysisId(), event.status());
    }
}
