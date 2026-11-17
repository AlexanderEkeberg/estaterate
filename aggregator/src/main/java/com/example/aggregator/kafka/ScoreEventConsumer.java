package com.example.aggregator.kafka;

import com.example.aggregator.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreEventConsumer {

    private final AnalysisService analysisService;

    @KafkaListener(
            topics = "score-events",
            groupId = "aggregator-score",
            properties = {
                    "spring.json.value.default.type=com.example.aggregator.kafka.ScoreEvent",
                    "spring.json.use.type.headers=false"
            }
    )
    public void receive(ScoreEvent event) {
        if (event == null || event.analysisId() == null) {
            log.atWarn()
                    .addKeyValue("event_type", "event_missing_analysis_id")
                    .addKeyValue("topic", "score-events")
                    .log("Event is missing analysisId");
            return;
        }

        log.atInfo()
                .addKeyValue("event_type", "score_received")
                .addKeyValue("analysis_id", event.analysisId())
                .addKeyValue("category", event.category())
                .addKeyValue("status", event.status())
                .log("Received score event");

        analysisService.processScoreEvent(event);
    }
}