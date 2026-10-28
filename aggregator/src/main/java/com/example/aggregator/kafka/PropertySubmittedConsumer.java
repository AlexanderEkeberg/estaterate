package com.example.aggregator.kafka;

import com.example.aggregator.service.AnalysisService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertySubmittedConsumer {

    private final AnalysisService analysisService;

    @KafkaListener(
            topics = "property-submitted",
            groupId = "aggregator-property",
            properties = {
                    "spring.json.value.default.type=com.example.aggregator.kafka.PropertySubmittedConsumer$PropertySubmittedEvent",
                    "spring.json.use.type.headers=false"
            }
    )
    public void receive(PropertySubmittedEvent event) {
        if (event == null || event.analysisId() == null) {
            log.warn("property-submitted missing analysisId");
            return;
        }

        log.info("Received property-submitted for analysisId={}", event.analysisId());
        analysisService.ensureAnalysisExists(event.analysisId());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PropertySubmittedEvent(
            UUID eventId,
            String eventType,
            int eventVersion,
            Instant occurredAt,
            String analysisId,
            String url,
            String postalCode,
            Double latitude,
            Double longitude,
            String coordinatesSource,
            String municipalityNumber,
            String district
    ) {}
}