package no.property.event;

public record PropertySubmittedEvent(
        String analysisId,
        String url,
        double lat,
        double lon,
        String postalCode,
        String municipalityNumber,
        String district
) {
}
