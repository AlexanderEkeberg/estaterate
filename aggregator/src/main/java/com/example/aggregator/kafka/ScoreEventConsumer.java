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
            log.warn("score-event missing analysisId");
            return;
        }

        log.info("Received score-event analysisId={}, category={}, status={}",
                event.analysisId(), event.category(), event.status());

        analysisService.processScoreEvent(event);
    }
}