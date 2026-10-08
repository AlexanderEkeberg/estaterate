package no.weather.events;

import java.time.Instant;
import java.util.UUID;

public record PropertySubmittedEvent(
        UUID eventId,
        String eventType,
        int eventVersion,
        UUID propertyId,
        String url,
        String postalCode,
        Double latitude,
        Double longitude,
        Instant occurredAt
) {
}