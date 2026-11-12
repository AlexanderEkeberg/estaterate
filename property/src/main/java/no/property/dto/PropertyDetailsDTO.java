package no.property.dto;

public record PropertyDetailsDTO(
        String address,
        String postalCode,
        Integer askingPrice,
        Integer usableArea
) {
}
