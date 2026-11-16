package org.example.crimerate.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.crimerate.events.ScoreEvent;
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
                    logFailed(event, error);
                } else {
                    log.atInfo()
                            .addKeyValue("event_type", "score_published")
                            .addKeyValue("analysis_id", event.analysisId())
                            .addKeyValue("category", event.category())
                            .addKeyValue("status", event.status())
                            .log("Sent score event");
                }
            });
        } catch (Exception e) {
            logFailed(event, e);
        }
    }

    private void logFailed(ScoreEvent event, Throwable error) {
        log.atError()
                .setCause(error)
                .addKeyValue("event_type", "score_publish_failed")
                .addKeyValue("analysis_id", event.analysisId())
                .addKeyValue("category", event.category())
                .log("Could not send score event");
    }
}
