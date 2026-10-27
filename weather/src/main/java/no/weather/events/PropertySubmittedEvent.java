package no.weather.events;

import java.time.Instant;
import java.util.UUID;

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
) {
}
