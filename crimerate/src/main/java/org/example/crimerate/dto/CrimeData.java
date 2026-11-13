package org.example.crimerate.dto;

public record CrimeData(
        DataSource source,
        String bydel,
        int year,
        int crimeCount,
        int population,
        double ratePer1000,
        double score
) {
}
