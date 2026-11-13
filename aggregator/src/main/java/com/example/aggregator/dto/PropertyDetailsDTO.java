package com.example.aggregator.dto;

public record PropertyDetailsDTO(
        String address,
        String postalCode,
        Integer askingPrice,
        Integer usableArea
) {
}