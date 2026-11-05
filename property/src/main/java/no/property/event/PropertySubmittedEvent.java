package no.property.event;

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
        double latitude,
        double longitude,
        CoordinatesSource coordinatesSource,
        String municipalityNumber,
        String district
) {
}
