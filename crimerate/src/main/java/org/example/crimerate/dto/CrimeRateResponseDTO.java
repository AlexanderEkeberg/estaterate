package org.example.crimerate.dto;

public record CrimeRateResponseDTO(
        String bydel,
        int year,
        int crimeCount,
        int population,
        double ratePer1000,
        double score
) {
}
