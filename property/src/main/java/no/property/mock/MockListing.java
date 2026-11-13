package no.property.mock;

record MockListing(
        String finnkode,
        String address,
        String postalCode,
        Double latitude,
        Double longitude,
        String municipalityNumber,
        String district,
        Integer askingPrice,
        Integer usableAreaInternal
) {
}
