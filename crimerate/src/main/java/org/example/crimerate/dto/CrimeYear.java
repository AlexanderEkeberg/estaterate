package org.example.crimerate.dto;

public record CrimeYear(
        int year,
        int crimeCount,
        int population,
        double ratePer1000,
        double osloRatePer1000,
        int rank,
        int districtCount
) {
}
